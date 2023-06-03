package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.getAllTypeIds
import com.example.util.simpletimetracker.domain.extension.getCommentItems
import com.example.util.simpletimetracker.domain.extension.getComments
import com.example.util.simpletimetracker.domain.extension.getDate
import com.example.util.simpletimetracker.domain.extension.getDaysOfWeek
import com.example.util.simpletimetracker.domain.extension.getDuration
import com.example.util.simpletimetracker.domain.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.extension.getManuallyFilteredRecordIds
import com.example.util.simpletimetracker.domain.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.extension.getTaggedIds
import com.example.util.simpletimetracker.domain.extension.getTimeOfDay
import com.example.util.simpletimetracker.domain.extension.getTypeIds
import com.example.util.simpletimetracker.domain.extension.hasAnyComment
import com.example.util.simpletimetracker.domain.extension.hasCategoryFilter
import com.example.util.simpletimetracker.domain.extension.hasNoComment
import com.example.util.simpletimetracker.domain.extension.hasUntaggedItem
import com.example.util.simpletimetracker.domain.extension.hasUntrackedFilter
import com.example.util.simpletimetracker.domain.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordFilterInteractor @Inject constructor(
    private val interactor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val getUntrackedRecordsInteractor: GetUntrackedRecordsInteractor,
    private val timeMapper: TimeMapper,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun mapDateFilter(
        rangeLength: RangeLength,
        rangePosition: Int,
    ): RecordsFilter? = withContext(Dispatchers.Default) {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift
        )

        return@withContext if (range.first == 0L && range.second == 0L) {
            null
        } else {
            RecordsFilter.Date(Range(range.first, range.second))
        }
    }

    suspend fun getByFilter(
        filters: List<RecordsFilter>,
    ): List<Record> = withContext(Dispatchers.Default) {
        if (filters.isEmpty()) return@withContext emptyList()

        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val calendar: Calendar = Calendar.getInstance()
        val typeIds: List<Long> = when {
            filters.hasCategoryFilter() -> {
                val types = recordTypeInteractor.getAll()
                val typeCategories = recordTypeCategoryInteractor.getAll()
                filters.getAllTypeIds(types, typeCategories)
            }
            else -> filters.getTypeIds()
        }
        val selectedCommentItems: List<RecordsFilter.CommentItem> = filters.getCommentItems()
        val comments: List<String> = selectedCommentItems.getComments().map(String::lowercase)
        val selectedNoComment: Boolean = selectedCommentItems.hasNoComment()
        val selectedAnyComment: Boolean = selectedCommentItems.hasAnyComment()
        val ranges: List<Range> = filters.getDate()?.let(::listOf).orEmpty()
        val selectedTagItems: List<RecordsFilter.TagItem> = filters.getSelectedTags()
        val selectedTaggedIds: List<Long> = selectedTagItems.getTaggedIds()
        val selectedUntagged: Boolean = selectedTagItems.hasUntaggedItem()
        val filteredTagItems: List<RecordsFilter.TagItem> = filters.getFilteredTags()
        val filteredTaggedIds: List<Long> = filteredTagItems.getTaggedIds()
        val filteredUntagged: Boolean = filteredTagItems.hasUntaggedItem()
        val manuallyFilteredIds: List<Long> = filters.getManuallyFilteredRecordIds()
        val daysOfWeek: List<DayOfWeek> = filters.getDaysOfWeek()
        val timeOfDay: Range? = filters.getTimeOfDay()
        val durations: List<Range> = filters.getDuration()?.let(::listOf).orEmpty()

        // TODO Use different queries for optimization.
        // TODO by tag (tagged, untagged).
        val records: List<Record> = when {
            filters.hasUntrackedFilter() -> {
                val range = ranges.firstOrNull() ?: Range(0, 0)
                val records = if (range.timeStarted == 0L && range.timeEnded == 0L) {
                    interactor.getAll()
                } else {
                    interactor.getFromRange(range)
                }.map { Range(it.timeStarted, it.timeEnded) }
                getUntrackedRecordsInteractor.get(range, records)
            }
            typeIds.isNotEmpty() && ranges.isNotEmpty() -> {
                val result = mutableMapOf<Long, Record>()
                ranges
                    .map { interactor.getFromRangeByType(typeIds, it) }
                    .flatten()
                    .forEach { result[it.id] = it }
                result.values.toList()
            }
            typeIds.isNotEmpty() && comments.isNotEmpty() -> {
                val result = mutableMapOf<Long, Record>()
                comments
                    .map { interactor.searchByTypeWithComment(typeIds, it) }
                    .flatten()
                    .forEach { result[it.id] = it }
                result.values.toList()
            }
            typeIds.isNotEmpty() -> {
                interactor.getByType(typeIds)
            }
            ranges.isNotEmpty() -> {
                val result = mutableMapOf<Long, Record>()
                ranges
                    .map { interactor.getFromRange(it) }
                    .flatten()
                    .forEach { result[it.id] = it }
                result.values.toList()
            }
            comments.isNotEmpty() -> {
                interactor.searchComment(comments.firstOrNull().orEmpty())
            }
            selectedAnyComment -> {
                interactor.searchAnyComments()
            }
            else -> interactor.getAll()
        }

        fun Record.selectedByActivity(): Boolean {
            return typeIds.isEmpty() || typeId in typeIds
        }

        fun Record.selectedByComment(): Boolean {
            if (selectedCommentItems.isEmpty()) return true
            val comment = this.comment.lowercase()
            return (selectedNoComment && comment.isEmpty()) ||
                (selectedAnyComment && comment.isNotEmpty()) ||
                (comment.isNotEmpty() && comments.any { comment.contains(it) })
        }

        fun Record.selectedByDate(): Boolean {
            if (ranges.isEmpty()) return true
            return ranges.any { range -> timeStarted < range.timeEnded && timeEnded > range.timeStarted }
        }

        fun Record.selectedByTag(): Boolean {
            if (selectedTagItems.isEmpty()) return true
            return if (tagIds.isNotEmpty()) {
                tagIds.any { tagId -> tagId in selectedTaggedIds }
            } else {
                selectedUntagged
            }
        }

        fun Record.filteredByTag(): Boolean {
            if (filteredTagItems.isEmpty()) return false
            return if (tagIds.isNotEmpty()) {
                tagIds.any { tagId -> tagId in filteredTaggedIds }
            } else {
                filteredUntagged
            }
        }

        fun Record.isManuallyFiltered(): Boolean {
            if (manuallyFilteredIds.isEmpty()) return false
            return id in manuallyFilteredIds
        }

        fun Record.selectedByDayOfWeek(): Boolean {
            if (daysOfWeek.isEmpty()) return true

            val daysOfRecord: MutableSet<DayOfWeek> = mutableSetOf()
            val startDay = timeMapper.getDayOfWeek(
                timestamp = timeStarted,
                calendar = calendar,
                startOfDayShift = startOfDayShift
            )
            val endDay = timeMapper.getDayOfWeek(
                timestamp = timeEnded,
                calendar = calendar,
                startOfDayShift = startOfDayShift
            )
            daysOfRecord.add(startDay)
            daysOfRecord.add(endDay)

            // Check long records.
            if (timeEnded - timeStarted > dayInMillis) {
                var check = true
                calendar.timeInMillis = timeStarted
                // Continuously add one day to time start until reach time ended.
                while (check && daysOfRecord.size != 7) {
                    calendar.apply {
                        add(Calendar.DATE, 1)
                        if (timeInMillis < timeEnded) {
                            timeInMillis -= startOfDayShift
                            get(Calendar.DAY_OF_WEEK)
                                .let(timeMapper::toDayOfWeek)
                                .let(daysOfRecord::add)
                            timeInMillis += startOfDayShift
                        } else {
                            check = false
                        }
                    }
                }
            }

            return daysOfRecord.any { it in daysOfWeek }
        }

        fun Record.selectedByTimeOfDay(): Boolean {
            if (timeOfDay == null) return true

            // Check empty range.
            if (timeOfDay.duration == 0L) return false
            // Check long records.
            if (duration > dayInMillis) return true

            val recordStart = timeStarted - timeMapper.getStartOfDayTimeStamp(timeStarted, calendar)
            val recordEnd = timeEnded - timeMapper.getStartOfDayTimeStamp(timeEnded, calendar)
            val recordRanges = if (recordStart <= recordEnd) {
                listOf(Range(recordStart, recordEnd))
            } else {
                listOf(Range(0, recordEnd), Range(recordStart, dayInMillis))
            }
            val timeOfDayRanges = if (timeOfDay.timeStarted <= timeOfDay.timeEnded) {
                listOf(Range(timeOfDay.timeStarted, timeOfDay.timeEnded))
            } else {
                listOf(Range(0, timeOfDay.timeEnded), Range(timeOfDay.timeStarted, dayInMillis))
            }

            return recordRanges.any { recordRange ->
                timeOfDayRanges.any { it.isOverlappingWith(recordRange) }
            }
        }

        fun Record.selectedByDuration(): Boolean {
            if (durations.isEmpty()) return true
            return durations.any { duration >= it.timeStarted && duration <= it.timeEnded }
        }

        return@withContext records.filter { record ->
            record.selectedByActivity() &&
                record.selectedByComment() &&
                record.selectedByDate() &&
                record.selectedByTag() &&
                !record.filteredByTag() &&
                !record.isManuallyFiltered() &&
                record.selectedByDayOfWeek() &&
                record.selectedByTimeOfDay() &&
                record.selectedByDuration()
        }
    }

    companion object {
        private val dayInMillis = TimeUnit.DAYS.toMillis(1)
    }
}