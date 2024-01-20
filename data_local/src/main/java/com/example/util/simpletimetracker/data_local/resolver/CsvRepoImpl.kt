package com.example.util.simpletimetracker.data_local.resolver

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.resolver.CsvRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class CsvRepoImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val recordTypeRepo: RecordTypeRepo,
    private val categoryRepo: CategoryRepo,
    private val recordRepo: RecordRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTagRepo: RecordTagRepo,
    private val resourceRepo: ResourceRepo,
) : CsvRepo {

    override suspend fun saveCsvFile(
        uriString: String,
        range: Range?,
    ): ResultCode = withContext(Dispatchers.IO) {
        var fileDescriptor: ParcelFileDescriptor? = null
        var fileOutputStream: FileOutputStream? = null

        try {
            val uri = Uri.parse(uriString)
            fileDescriptor = contentResolver.openFileDescriptor(uri, "wt")
            fileOutputStream = fileDescriptor?.fileDescriptor?.let(::FileOutputStream)

            // Write csv header
            fileOutputStream?.write(CSV_HEADER.toByteArray())

            val recordTypes = recordTypeRepo.getAll().associateBy { it.id }
            val categories = categoryRepo.getAll().associateBy { it.id }
            val recordTags = recordTagRepo.getAll()
            val typeToCategories = recordTypes.map { (id, _) ->
                id to recordTypeCategoryRepo.getCategoryIdsByType(id).mapNotNull { categories[it] }
            }.toMap()

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
                        record = record,
                        recordType = recordTypes[record.typeId],
                        categories = typeToCategories[record.typeId].orEmpty(),
                        recordTags = recordTags.filter { it.id in record.tagIds }
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
            } catch (e: IOException) {
                // Do nothing
            }
        }
    }

    override suspend fun importCsvFile(
        uriString: String
    ): ResultCode = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var reader: BufferedReader? = null

        try {
            val uri = Uri.parse(uriString)
            inputStream = contentResolver.openInputStream(uri)
            reader = inputStream?.let(::InputStreamReader)?.let(::BufferedReader)

            var line = ""
            var addedRecords = 0L
            val currentTypes = recordTypeRepo.getAll()
            val newAddedTypes = mutableListOf<RecordType>()

            // Read data
            while (reader?.readLine()?.also { line = it } != null) {
                line = line.removePrefix("\"")
                val typeName = line.substringBefore(delimiter = "\"", missingDelimiterValue = "")
                line = line.removePrefix("$typeName\",")

                val timeStartedString = line.substringBefore(delimiter = ",", missingDelimiterValue = "")
                val timeStarted = parseDateTime(timeStartedString)
                line = line.removePrefix("$timeStartedString,")

                val timeEndedString = line.substringBefore(delimiter = ",", missingDelimiterValue = "")
                val timeEnded = parseDateTime(timeEndedString)
                line = line.removePrefix("$timeEndedString,")

                line = line.removePrefix("\"")
                val comment = line.substringBefore(delimiter = "\"", missingDelimiterValue = "")

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
            } catch (e: IOException) {
                // Do nothing
            }
        }
    }

    private fun toCsvString(
        record: Record,
        recordType: RecordType?,
        categories: List<Category>,
        recordTags: List<RecordTag>,
    ): String? {
        return if (recordType != null) {
            String.format(
                "\"%s\",%s,%s,\"%s\",\"%s\",\"%s\",%s,%s\n",
                recordType.name,
                formatDateTime(record.timeStarted),
                formatDateTime(record.timeEnded),
                record.comment,
                categories.takeUnless { it.isEmpty() }?.joinToString(separator = ", ") { it.name }.orEmpty(),
                recordTags.takeUnless { it.isEmpty() }?.joinToString(separator = ", ") { it.name }.orEmpty(),
                formatDuration(record.timeEnded - record.timeStarted),
                formatDurationMinutes(record.timeEnded - record.timeStarted),
            )
        } else {
            null
        }
    }

    private fun formatDateTime(timestamp: Long): String {
        synchronized(dateTimeFormat) {
            return dateTimeFormat.format(timestamp)
        }
    }

    private fun formatDurationMinutes(interval: Long): String {
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(interval)
        return min.toString()
    }

    private fun formatDuration(interval: Long): String {
        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr)
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)
        )

        return "$hr:$min:$sec"
    }

    private fun parseDateTime(timeString: String): Long? {
        return synchronized(dateTimeFormat) {
            runCatching {
                dateTimeFormat.parse(timeString)
            }.getOrNull()?.time
        }
    }

    companion object {
        private const val CSV_HEADER = "activity name,time started,time ended,comment,categories,record tags,duration,duration minutes\n"

        private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}