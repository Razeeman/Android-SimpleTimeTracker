package com.example.util.simpletimetracker.data_local.resolver

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.resolver.CsvRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CsvRepoImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val recordTypeRepo: RecordTypeRepo,
    private val recordRepo: RecordRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTagRepo: RecordTagRepo,
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
            val recordTags = recordTagRepo.getAll()
            val categories = recordTypes.map { (id, _) ->
                id to recordTypeCategoryRepo.getCategoriesByType(id)
            }.toMap()

            // Write data
            val records = if (range != null) {
                recordRepo.getFromRange(range.timeStarted, range.timeEnded)
            } else {
                recordRepo.getAll()
            }
            records
                .sortedBy { it.timeStarted }
                .forEach { record ->
                    toCsvString(
                        record = record,
                        recordType = recordTypes[record.typeId],
                        categories = categories[record.typeId].orEmpty(),
                        recordTags = recordTags.filter { it.id in record.tagIds }
                    )
                        ?.toByteArray()
                        ?.let { fileOutputStream?.write(it) }
                }

            fileOutputStream?.close()
            fileDescriptor?.close()
            ResultCode.SUCCESS
        } catch (e: Exception) {
            Timber.e(e)
            ResultCode.ERROR
        } finally {
            try {
                fileOutputStream?.close()
                fileDescriptor?.close()
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

    companion object {
        private const val CSV_HEADER = "activity name,time started,time ended,comment,categories,record tags,duration,duration minutes\n"

        private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}