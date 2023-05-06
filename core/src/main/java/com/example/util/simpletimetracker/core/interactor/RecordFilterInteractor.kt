package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.getAllTypeIds
import com.example.util.simpletimetracker.domain.extension.getCommentItems
import com.example.util.simpletimetracker.domain.extension.getComments
import com.example.util.simpletimetracker.domain.extension.getDate
import com.example.util.simpletimetracker.domain.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.extension.getManuallyFilteredRecordIds
import com.example.util.simpletimetracker.domain.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.extension.getTaggedIds
import com.example.util.simpletimetracker.domain.extension.getTypeIds
import com.example.util.simpletimetracker.domain.extension.hasAnyComment
import com.example.util.simpletimetracker.domain.extension.hasCategoryFilter
import com.example.util.simpletimetracker.domain.extension.hasNoComment
import com.example.util.simpletimetracker.domain.extension.hasUntaggedItem
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import javax.inject.Inject

class RecordFilterInteractor @Inject constructor(
    private val interactor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val timeMapper: TimeMapper,
    private val prefsInteractor: PrefsInteractor,
) {

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
                    .map { interactor.getFromRange(it.timeStarted, it.timeEnded) }
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

        return records.filter { record ->
            record.selectedByActivity() &&
                record.selectedByComment() &&
                record.selectedByDate() &&
                record.selectedByTag() &&
                !record.filteredByTag() &&
                !record.isManuallyFiltered()
        }
    }
}