package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.getAllTypeIds
import com.example.util.simpletimetracker.domain.extension.getComment
import com.example.util.simpletimetracker.domain.extension.getDate
import com.example.util.simpletimetracker.domain.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.extension.getManuallyFilteredRecordIds
import com.example.util.simpletimetracker.domain.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.extension.getTaggedIds
import com.example.util.simpletimetracker.domain.extension.getUntaggedIds
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import javax.inject.Inject

class RecordFilterInteractor @Inject constructor(
    private val interactor: RecordInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val timeMapper: TimeMapper,
    private val prefsInteractor: PrefsInteractor,
) {

    fun mapFilter(
        filterType: ChartFilterType,
        selectedIds: List<Long>,
    ): List<RecordsFilter> {
        val filters = when (filterType) {
            ChartFilterType.ACTIVITY -> {
                RecordsFilter.Activity(selectedIds)
                    .takeUnless { selectedIds.isEmpty() }
                    .let(::listOfNotNull)
            }
            ChartFilterType.CATEGORY -> {
                RecordsFilter.Category(selectedIds)
                    .takeUnless { selectedIds.isEmpty() }
                    .let(::listOfNotNull)
            }
            ChartFilterType.RECORD_TAG -> {
                selectedIds
                    .takeUnless { it.isEmpty() }
                    ?.map(RecordsFilter.Tag::Tagged)
                    ?.let(RecordsFilter::SelectedTags)
                    .let(::listOfNotNull)
            }
        }

        return filters
    }

    suspend fun mapDateFilter(
        rangeLength: RangeLength,
        rangePosition: Int,
    ): RecordsFilter? {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift
        )

        return if (range.first == 0L && range.second == 0L) {
            null
        } else {
            RecordsFilter.Date(Range(range.first, range.second))
        }
    }

    suspend fun getByFilter(filters: List<RecordsFilter>): List<Record> {
        if (filters.isEmpty()) return emptyList()

        val typeIds: List<Long> = filters.getAllTypeIds(recordTypeCategoryInteractor.getAll())
        val comments: List<String> = filters.getComment()?.lowercase()?.let(::listOf).orEmpty()
        val ranges: List<Range> = filters.getDate()?.let(::listOf).orEmpty()
        val selectedTags: List<RecordsFilter.Tag> = filters.getSelectedTags()
        val selectedTaggedIds: List<Long> = selectedTags.getTaggedIds()
        val selectedUntaggedIds: List<Long> = selectedTags.getUntaggedIds()
        val filteredTags: List<RecordsFilter.Tag> = filters.getFilteredTags()
        val filteredTaggedIds: List<Long> = filteredTags.getTaggedIds()
        val filteredUntaggedIds: List<Long> = filteredTags.getUntaggedIds()
        val manuallyFilteredIds: List<Long> = filters.getManuallyFilteredRecordIds()

        // Use different queries for optimization.
        // TODO by tag (tagged, untagged).
        val records: List<Record> = when {
            typeIds.isNotEmpty() && ranges.isNotEmpty() -> {
                val result = mutableMapOf<Long, Record>()
                ranges
                    .map { interactor.getFromRangeByType(typeIds, it.timeStarted, it.timeEnded) }
                    .flatten()
                    .forEach { result[it.id] = it }
                result.values.toList()
            }
            typeIds.isNotEmpty() && comments.isNotEmpty() -> {
                val result = mutableMapOf<Long, Record>()
                comments
                    .map { interactor.searchComments(typeIds, it) }
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
                    .map { interactor.getFromRange(it.timeStarted, it.timeEnded) }
                    .flatten()
                    .forEach { result[it.id] = it }
                result.values.toList()
            }
            else -> interactor.getAll()
        }

        fun Record.selectedByActivity(): Boolean {
            return typeIds.isEmpty() || typeId in typeIds
        }

        fun Record.selectedByComment(): Boolean {
            if (comments.isEmpty()) return true
            val comment = this.comment.lowercase()
            return comments.any { comment.contains(it) }
        }

        fun Record.selectedByDate(): Boolean {
            if (ranges.isEmpty()) return true
            return ranges.any { range -> timeStarted < range.timeEnded && timeEnded > range.timeStarted }
        }

        fun Record.selectedByTag(): Boolean {
            if (selectedTags.isEmpty()) return true
            return if (tagIds.isNotEmpty()) {
                tagIds.any { tagId -> tagId in selectedTaggedIds }
            } else {
                typeId in selectedUntaggedIds
            }
        }

        fun Record.filteredByTag(): Boolean {
            if (filteredTags.isEmpty()) return false
            return if (tagIds.isNotEmpty()) {
                tagIds.any { tagId -> tagId in filteredTaggedIds }
            } else {
                typeId in filteredUntaggedIds
            }
        }

        fun Record.isManuallyFiltered(): Boolean {
            if (manuallyFilteredIds.isEmpty()) return false
            return id in manuallyFilteredIds
        }

        return records.filter { record ->
            record.selectedByActivity() &&
                record.selectedByComment() &&
                record.selectedByDate() &&
                record.selectedByTag() &&
                (record.selectedByTag() && !record.filteredByTag()) &&
                !record.isManuallyFiltered()
        }
    }
}