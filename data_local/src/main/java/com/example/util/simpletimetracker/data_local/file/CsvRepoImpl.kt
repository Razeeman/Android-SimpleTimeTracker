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
import java.io.BufferedReader
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
            fileOutputStream?.write(CSV_HEADER.toByteArray())

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
                        ?.toByteArray()
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
        var reader: BufferedReader? = null

        try {
            val uri = uriString.toUri()
            inputStream = contentResolver.openInputStream(uri)
            reader = inputStream?.let(::InputStreamReader)?.let(::BufferedReader)

            var line = ""
            var nextPart: String
            var addedRecords = 0L
            val currentTypes = recordTypeRepo.getAll()
            val newAddedTypes = mutableListOf<RecordType>()

            // Read data
            while (reader?.readLine()?.also { line = it } != null) {
                val typeName: String
                if (line.startsWith(QUOTE)) {
                    line = line.removePrefix("\"")
                    typeName = line.substringBefore(delimiter = QUOTE, missingDelimiterValue = "")
                    nextPart = line.removePrefix("$typeName$QUOTE,")
                } else {
                    typeName = line.substringBefore(delimiter = ",", missingDelimiterValue = "")
                    nextPart = line.removePrefix("$typeName,")
                }
                line = nextPart

                val timeStartedString = line.substringBefore(delimiter = ",", missingDelimiterValue = "")
                val timeStarted = parseDateTime(timeStartedString)
                line = line.removePrefix("$timeStartedString,")

                val timeEndedString = line.substringBefore(delimiter = ",", missingDelimiterValue = "")
                val timeEnded = parseDateTime(timeEndedString)
                line = line.removePrefix("$timeEndedString,")

                val comment: String
                if (line.startsWith(QUOTE)) {
                    line = line.removePrefix(QUOTE)
                    comment = line.substringBefore(delimiter = QUOTE, missingDelimiterValue = "")
                } else {
                    // If comment is last column and there is no comma - take whole line.
                    comment = line.substringBefore(delimiter = ",", missingDelimiterValue = line)
                }

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
                                    colorId = (0..ColorMapper.colorsNumber).random(),
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
                reader?.close()
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
            String.format(
                "\"%s\",%s,%s,\"%s\",\"%s\",\"%s\",%s,%s\n",
                recordType.name.cleanText(),
                formatDateTime(dateTimeFormat, record.timeStarted),
                formatDateTime(dateTimeFormat, record.timeEnded),
                record.comment.cleanText(),
                categories
                    .joinToString(separator = ", ", transform = { it.name })
                    .cleanText(),
                recordTagFullNameMapper.getFullName(
                    tags = recordTags,
                    tagData = recordTagsData,
                ).cleanText(),
                formatDuration(record.duration),
                formatDurationMinutes(record.duration),
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

    private fun String.cleanText(): String {
        return this.replace("\"", "\"\"")
    }

    companion object {
        private const val QUOTE = "\""
        private val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        private const val CSV_HEADER =
            "activity name,time started,time ended,comment,categories,record tags,duration,duration minutes\n"
    }
}