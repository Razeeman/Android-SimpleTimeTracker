package com.example.util.simpletimetracker.data_local.resolver

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.data_local.R
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.resolver.IcsRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

class IcsRepoImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val recordTypeRepo: RecordTypeRepo,
    private val recordRepo: RecordRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTagRepo: RecordTagRepo,
    private val resourceRepo: ResourceRepo,
) : IcsRepo {

    private val commentTitle: String by lazy {
        resourceRepo.getString(R.string.change_record_comment_field)
    }
    private val categoryTitle: String by lazy {
        resourceRepo.getString(R.string.category_hint)
    }
    private val tagsTitle: String by lazy {
        resourceRepo.getString(R.string.record_tag_hint)
    }

    override suspend fun saveIcsFile(
        uriString: String,
        range: Range?,
    ): ResultCode = withContext(Dispatchers.IO) {
        var fileDescriptor: ParcelFileDescriptor? = null
        var fileOutputStream: FileOutputStream? = null

        try {
            val uri = Uri.parse(uriString)
            fileDescriptor = contentResolver.openFileDescriptor(uri, "w")
            fileOutputStream = fileDescriptor?.fileDescriptor?.let(::FileOutputStream)

            // Write ics header
            fileOutputStream?.write(ICS_HEADER.toByteArray())

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
                    toIcsString(
                        record = record,
                        recordType = recordTypes[record.typeId],
                        categories = categories[record.typeId].orEmpty(),
                        recordTags = recordTags.filter { it.id in record.tagIds }
                    )
                        ?.toByteArray()
                        ?.let { fileOutputStream?.write(it) }
                }

            // Write ics footer
            fileOutputStream?.write(ICS_FOOTER.toByteArray())

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

    private fun toIcsString(
        record: Record,
        recordType: RecordType?,
        categories: List<Category>,
        recordTags: List<RecordTag>,
    ): String? {
        if (recordType == null) return null

        val commentString = record.comment.clean()
            .wrapText(commentTitle)
        val categoriesString = categories.joinToString(separator = ", ") { it.name.clean() }
            .wrapText(categoryTitle)
        val tagsString = recordTags.joinToString(separator = ", ") { it.name.clean() }
            .wrapText(tagsTitle)
        val description = commentString + categoriesString + tagsString

        return StringBuilder()
            .append("BEGIN:VEVENT\n")
            .append("DTSTART:${formatDateTime(record.timeStarted)}\n")
            .append("DTEND:${formatDateTime(record.timeEnded)}\n")
            .append("UID:${UUID.randomUUID()}@stt\n")
            .append("SUMMARY:${recordType.name.clean()}\n")
            .append("DESCRIPTION:$description\n")
            .append("END:VEVENT\n")
            .toString()
    }

    private fun formatDateTime(timestamp: Long): String {
        synchronized(dateTimeFormat) {
            return dateTimeFormat.format(timestamp)
        }
    }

    private fun String.wrapText(
        title: String,
    ): String = this
        .takeUnless { it.isEmpty() }
        ?.let { "$title: $it\\n" }
        .orEmpty()

    private fun String.clean() =
        replace("\n", "\\n")

    companion object {
        private const val ICS_HEADER = "BEGIN:VCALENDAR\n" +
            "PRODID:-//Simple Time Tracker//EN\n" +
            "VERSION:2.0\n"
        private const val ICS_FOOTER = "END:VCALENDAR"

        private val dateTimeFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
    }
}