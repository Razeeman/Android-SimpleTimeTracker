package com.example.util.simpletimetracker.data_local.file

import android.content.ContentResolver
import android.os.ParcelFileDescriptor
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.category.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.record.repo.RecordRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.category.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.recordType.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.backup.repo.CsvRepo
import com.example.util.simpletimetracker.domain.backup.model.ResultCode
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedOutputStream
import androidx.core.net.toUri
import com.example.util.simpletimetracker.core.mapper.FileExportDateTimeFormatMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagFullNameMapper
import com.example.util.simpletimetracker.domain.fileExport.ExportDateTimeFormat
import com.example.util.simpletimetracker.domain.record.model.RecordBase

class CsvRepoImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val recordTypeRepo: RecordTypeRepo,
    private val categoryRepo: CategoryRepo,
    private val recordRepo: RecordRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTagRepo: RecordTagRepo,
    private val resourceRepo: ResourceRepo,
    private val recordTagFullNameMapper: RecordTagFullNameMapper,
    private val fileExportDateTimeFormatMapper: FileExportDateTimeFormatMapper,
) : CsvRepo {

    override suspend fun saveCsvFile(
        uriString: String,
        range: Range?,
        dateTimeFormat: ExportDateTimeFormat,
    ): ResultCode = withContext(Dispatchers.IO) {
        var fileDescriptor: ParcelFileDescriptor? = null
        var fileOutputStream: BufferedOutputStream? = null

        try {
            val uri = uriString.toUri()
            fileDescriptor = contentResolver.openFileDescriptor(uri, "wt")
            fileOutputStream = fileDescriptor?.fileDescriptor
                ?.let(::FileOutputStream)?.buffered()

            // Write UTF-8 BOM so Excel can detect the CSV encoding correctly.
            fileOutputStream?.write(UTF8_BOM)

            // Write csv header
            fileOutputStream?.write(CSV_HEADER.toByteArray(Charsets.UTF_8))

            val recordTypes = recordTypeRepo.getAll().associateBy { it.id }
            val categories = categoryRepo.getAll().associateBy { it.id }
            val recordTags = recordTagRepo.getAll().associateBy { it.id }
            val typeToCategories = recordTypeCategoryRepo.getAll()
                .groupBy(RecordTypeCategory::recordTypeId)
                .mapValues { (_, relations) -> relations.mapNotNull { categories[it.categoryId] } }

            // Write data
            val records = if (range != null) {
                recordRepo.getFromRange(range)
            } else {
                recordRepo.getAll()
            }
            records
                .sortedBy { it.timeStarted }
                .forEach { record ->
                    toCsvString(
                        dateTimeFormat = dateTimeFormat,
                        record = record,
                        recordType = recordTypes[record.typeId],
                        categories = typeToCategories[record.typeId].orEmpty(),
                        recordTags = record.tags.mapNotNull { recordTags[it.tagId] },
                        recordTagsData = record.tags,
                    )
                        ?.toByteArray(Charsets.UTF_8)
                        ?.let { fileOutputStream?.write(it) }
                }

            fileOutputStream?.close()
            fileDescriptor?.close()
            ResultCode.Success(resourceRepo.getString(R.string.message_export_complete))
        } catch (e: Exception) {
            Timber.e(e)
            ResultCode.Error(resourceRepo.getString(R.string.message_export_error))
        } finally {
            try {
                fileOutputStream?.close()
                fileDescriptor?.close()
            } catch (_: IOException) {
                // Do nothing
            }
        }
    }

    override suspend fun importCsvFile(
        uriString: String,
    ): ResultCode = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null

        try {
            val uri = uriString.toUri()
            inputStream = contentResolver.openInputStream(uri)
            val reader = inputStream
                ?.let { InputStreamReader(it, Charsets.UTF_8) }
                ?.let(::CsvReader)

            var addedRecords = 0L
            val currentTypes = recordTypeRepo.getAll()
            val newAddedTypes = mutableListOf<RecordType>()

            // Read data
            while (true) {
                val columns = reader?.readRow() ?: break
                if (columns.size < IMPORT_COLUMN_COUNT) continue

                val typeName = columns[0]
                val timeStarted = parseDateTime(columns[1])
                val timeEnded = parseDateTime(columns[2])
                val comment = columns[3]

                if (
                    typeName.isNotEmpty() &&
                    timeStarted != null &&
                    timeEnded != null
                ) {
                    val typeId: Long = currentTypes.firstOrNull { it.name == typeName }?.id
                        ?: newAddedTypes.firstOrNull { it.name == typeName }?.id
                        ?: run {
                            val newType = RecordType(
                                name = typeName,
                                icon = "",
                                color = AppColor(
                                    colorId = ColorMapper.getAvailableColors().indices.random(),
                                    colorInt = "",
                                ),
                                hidden = false,
                                defaultDuration = 0L,
                                note = "",
                            )
                            val newTypeId = recordTypeRepo.add(newType)
                            newType.copy(id = newTypeId).let(newAddedTypes::add)
                            newTypeId
                        }
                    val record = Record(
                        typeId = typeId,
                        timeStarted = timeStarted,
                        timeEnded = timeEnded,
                        comment = comment,
                        tags = emptyList(),
                    )
                    recordRepo.add(record)
                    addedRecords++
                }
            }
            val messageText = resourceRepo.getString(R.string.message_import_complete)
            val messageHint = resourceRepo.getString(R.string.message_import_complete_hint, addedRecords)
            ResultCode.Success("$messageText\n$messageHint")
        } catch (e: Exception) {
            Timber.e(e)
            ResultCode.Error(resourceRepo.getString(R.string.message_import_error))
        } finally {
            try {
                inputStream?.close()
            } catch (_: IOException) {
                // Do nothing
            }
        }
    }

    private fun toCsvString(
        dateTimeFormat: ExportDateTimeFormat,
        record: Record,
        recordType: RecordType?,
        categories: List<Category>,
        recordTags: List<RecordTag>,
        recordTagsData: List<RecordBase.Tag>,
    ): String? {
        return if (recordType != null) {
            CsvWriter.writeRow(
                listOf(
                    recordType.name,
                    formatDateTime(dateTimeFormat, record.timeStarted),
                    formatDateTime(dateTimeFormat, record.timeEnded),
                    record.comment,
                    categories.joinToString(separator = ", ", transform = { it.name }),
                    recordTagFullNameMapper.getFullName(
                        tags = recordTags,
                        tagData = recordTagsData,
                    ),
                    formatDuration(record.duration),
                    formatDurationMinutes(record.duration),
                ),
            )
        } else {
            null
        }
    }

    private fun formatDateTime(
        format: ExportDateTimeFormat,
        timestamp: Long,
    ): String {
        return fileExportDateTimeFormatMapper.mapDateTime(
            format = format,
            timestamp = timestamp,
        )
    }

    private fun formatDurationMinutes(interval: Long): String {
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(interval)
        return min.toString()
    }

    private fun formatDuration(interval: Long): String {
        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval,
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr),
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min),
        )

        return "$hr:$min:$sec"
    }

    private fun parseDateTime(timeString: String): Long? {
        fileExportDateTimeFormatMapper.getAvailableFormats().forEach { format ->
            val result = fileExportDateTimeFormatMapper.parseDateTime(format, timeString)
            if (result != null) return result
        }
        return null
    }

    companion object {
        private const val IMPORT_COLUMN_COUNT = 4
        private val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        private const val CSV_HEADER =
            "activity name,time started,time ended,comment,categories,record tags,duration,duration minutes\r\n"
    }
}