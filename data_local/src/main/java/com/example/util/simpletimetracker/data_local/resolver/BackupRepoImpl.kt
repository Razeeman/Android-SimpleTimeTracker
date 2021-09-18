package com.example.util.simpletimetracker.data_local.resolver

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordToRecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Do not change backup parts order, always add new to the end.
 * Otherwise previous version's backups will be broken.
 */
@Singleton
class BackupRepoImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val recordTypeRepo: RecordTypeRepo,
    private val recordTypeCacheRepo: RecordTypeCacheRepo,
    private val recordRepo: RecordRepo,
    private val recordCacheRepo: RecordCacheRepo,
    private val categoryRepo: CategoryRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val recordTagRepo: RecordTagRepo,
) : BackupRepo {

    override suspend fun saveBackupFile(uriString: String): BackupRepo.ResultCode =
        withContext(Dispatchers.IO) {
            var fileDescriptor: ParcelFileDescriptor? = null
            var fileOutputStream: FileOutputStream? = null

            try {
                val uri = Uri.parse(uriString)
                fileDescriptor = contentResolver.openFileDescriptor(uri, "w")
                fileOutputStream = fileDescriptor?.fileDescriptor?.let(::FileOutputStream)

                // Write file identification
                val identificationBackupRow: String = BACKUP_IDENTIFICATION + "\n"
                fileOutputStream?.write(identificationBackupRow.toByteArray())

                // Write data
                recordTypeRepo.getAll().forEach {
                    fileOutputStream?.write(
                        it.let(::toBackupString).toByteArray()
                    )
                }
                recordRepo.getAll().forEach {
                    fileOutputStream?.write(
                        it.let(::toBackupString).toByteArray()
                    )
                }
                categoryRepo.getAll().forEach {
                    fileOutputStream?.write(
                        it.let(::toBackupString).toByteArray()
                    )
                }
                recordTypeCategoryRepo.getAll().forEach {
                    fileOutputStream?.write(
                        it.let(::toBackupString).toByteArray()
                    )
                }
                recordTagRepo.getAll().forEach {
                    fileOutputStream?.write(
                        it.let(::toBackupString).toByteArray()
                    )
                }
                recordToRecordTagRepo.getAll().forEach {
                    fileOutputStream?.write(
                        it.let(::toBackupString).toByteArray()
                    )
                }

                fileOutputStream?.close()
                fileDescriptor?.close()
                BackupRepo.ResultCode.SUCCESS
            } catch (e: Exception) {
                Timber.e(e)
                BackupRepo.ResultCode.ERROR
            } finally {
                try {
                    fileOutputStream?.close()
                    fileDescriptor?.close()
                } catch (e: IOException) {
                    // Do nothing
                }
            }
        }

    override suspend fun restoreBackupFile(uriString: String): BackupRepo.ResultCode =
        withContext(Dispatchers.IO) {
            var inputStream: InputStream? = null
            var reader: BufferedReader? = null

            try {
                val uri = Uri.parse(uriString)
                inputStream = contentResolver.openInputStream(uri)
                reader = inputStream?.let(::InputStreamReader)?.let(::BufferedReader)

                var line: String
                var parts: List<String>

                // Check file identification
                line = reader?.readLine().orEmpty()
                if (line != BACKUP_IDENTIFICATION) return@withContext BackupRepo.ResultCode.ERROR

                recordTypeRepo.clear()
                recordTypeCacheRepo.clear()
                recordRepo.clear()
                recordCacheRepo.clear()
                categoryRepo.clear()
                recordTypeCategoryRepo.clear()
                recordTagRepo.clear()
                recordToRecordTagRepo.clear()

                // Read data
                while (reader?.readLine()?.also { line = it } != null) {
                    parts = line.split("\t")
                    when (parts[0]) {
                        ROW_RECORD_TYPE -> {
                            recordTypeFromBackupString(parts).let {
                                recordTypeRepo.add(it)
                            }
                        }
                        ROW_RECORD -> {
                            recordFromBackupString(parts).let {
                                recordRepo.add(it)
                            }
                        }
                        ROW_CATEGORY -> {
                            categoryFromBackupString(parts).let {
                                categoryRepo.add(it)
                            }
                        }
                        ROW_TYPE_CATEGORY -> {
                            typeCategoryFromBackupString(parts).let {
                                recordTypeCategoryRepo.add(it)
                            }
                        }
                        ROW_RECORD_TAG -> {
                            recordTagFromBackupString(parts).let {
                                recordTagRepo.add(it)
                            }
                        }
                        ROW_RECORD_TO_RECORD_TAG -> {
                            recordToRecordTagFromBackupString(parts).let {
                                recordToRecordTagRepo.add(it)
                            }
                        }
                    }
                }
                BackupRepo.ResultCode.SUCCESS
            } catch (e: Exception) {
                Timber.e(e)
                BackupRepo.ResultCode.ERROR
            } finally {
                try {
                    inputStream?.close()
                    reader?.close()
                } catch (e: IOException) {
                    // Do nothing
                }
            }
        }

    private fun toBackupString(recordType: RecordType): String {
        return String.format(
            "$ROW_RECORD_TYPE\t%s\t%s\t%s\t%s\t%s\t%s\n",
            recordType.id.toString(),
            recordType.name.clean(),
            recordType.icon,
            recordType.color.toString(),
            (if (recordType.hidden) 1 else 0).toString(),
            recordType.goalTime.toString()
        )
    }

    private fun toBackupString(record: Record): String {
        return String.format(
            "$ROW_RECORD\t%s\t%s\t%s\t%s\t%s\t%s\n",
            record.id.toString(),
            record.typeId.toString(),
            record.timeStarted.toString(),
            record.timeEnded.toString(),
            record.comment.clean(),
            record.tagId.toString()
        )
    }

    private fun toBackupString(category: Category): String {
        return String.format(
            "$ROW_CATEGORY\t%s\t%s\t%s\n",
            category.id.toString(),
            category.name.clean(),
            category.color.toString()
        )
    }

    private fun toBackupString(recordTypeCategory: RecordTypeCategory): String {
        return String.format(
            "$ROW_TYPE_CATEGORY\t%s\t%s\n",
            recordTypeCategory.recordTypeId.toString(),
            recordTypeCategory.categoryId.toString()
        )
    }

    private fun toBackupString(recordTag: RecordTag): String {
        return String.format(
            "$ROW_RECORD_TAG\t%s\t%s\t%s\t%s\t%s\n",
            recordTag.id.toString(),
            recordTag.typeId.toString(),
            recordTag.name.clean(),
            (if (recordTag.archived) 1 else 0).toString(),
            recordTag.color.toString(),
        )
    }

    private fun toBackupString(recordToRecordTag: RecordToRecordTag): String {
        return String.format(
            "$ROW_RECORD_TO_RECORD_TAG\t%s\t%s\n",
            recordToRecordTag.recordId.toString(),
            recordToRecordTag.recordTagId.toString()
        )
    }

    private fun recordTypeFromBackupString(parts: List<String>): RecordType {
        return RecordType(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            name = parts.getOrNull(2).orEmpty(),
            icon = parts.getOrNull(3).orEmpty(),
            color = parts.getOrNull(4)?.toIntOrNull().orZero(),
            hidden = parts.getOrNull(5)?.toIntOrNull() == 1,
            goalTime = parts.getOrNull(6)?.toLongOrNull().orZero()
        )
    }

    private fun recordFromBackupString(parts: List<String>): Record {
        return Record(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            typeId = parts.getOrNull(2)?.toLongOrNull() ?: 1L,
            timeStarted = parts.getOrNull(3)?.toLongOrNull().orZero(),
            timeEnded = parts.getOrNull(4)?.toLongOrNull().orZero(),
            comment = parts.getOrNull(5).orEmpty(),
            tagId = parts.getOrNull(6)?.toLongOrNull().orZero()
        )
    }

    private fun categoryFromBackupString(parts: List<String>): Category {
        return Category(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            name = parts.getOrNull(2).orEmpty(),
            color = parts.getOrNull(3)?.toIntOrNull().orZero()
        )
    }

    private fun typeCategoryFromBackupString(parts: List<String>): RecordTypeCategory {
        return RecordTypeCategory(
            recordTypeId = parts.getOrNull(1)?.toLongOrNull().orZero(),
            categoryId = parts.getOrNull(2)?.toLongOrNull().orZero()
        )
    }

    private fun recordTagFromBackupString(parts: List<String>): RecordTag {
        return RecordTag(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            typeId = parts.getOrNull(2)?.toLongOrNull().orZero(),
            name = parts.getOrNull(3).orEmpty(),
            archived = parts.getOrNull(4)?.toIntOrNull() == 1,
            color = parts.getOrNull(5)?.toIntOrNull().orZero(),
        )
    }

    private fun recordToRecordTagFromBackupString(parts: List<String>): RecordToRecordTag {
        return RecordToRecordTag(
            recordId = parts.getOrNull(1)?.toLongOrNull().orZero(),
            recordTagId = parts.getOrNull(2)?.toLongOrNull().orZero()
        )
    }

    private fun String.clean() =
        replace("[\n\t]".toRegex(), " ")

    companion object {
        private const val BACKUP_IDENTIFICATION = "app simple time tracker"
        private const val ROW_RECORD_TYPE = "recordType"
        private const val ROW_RECORD = "record"
        private const val ROW_CATEGORY = "category"
        private const val ROW_TYPE_CATEGORY = "typeCategory"
        private const val ROW_RECORD_TAG = "recordTag"
        private const val ROW_RECORD_TO_RECORD_TAG = "recordToRecordTag"
    }
}