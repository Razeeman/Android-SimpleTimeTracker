package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams
import javax.inject.Inject

class RecordFilterInteractor @Inject constructor(
    private val interactor: RecordInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
) {

    suspend fun mapFilter(filter: TypesFilterParams): List<RecordsFilter> {
        return when (filter.filterType) {
            ChartFilterType.ACTIVITY -> {
                RecordsFilter.Activity(filter.selectedIds)
                    .let(::listOf)
            }
            ChartFilterType.CATEGORY -> {
                recordTypeCategoryInteractor.getAll()
                    .filter { it.categoryId in filter.selectedIds }
                    .map { it.recordTypeId }
                    .distinct()
                    .let(RecordsFilter::Activity)
                    .let(::listOf)
            }
            ChartFilterType.RECORD_TAG -> {
                filter.selectedIds
                    .map(RecordsFilter.Tag::Tagged)
                    .let(RecordsFilter::SelectedTags)
                    .let(::listOf)
            }
        }.let {
            val filterTags = filter.filteredRecordTags.map {
                when (it) {
                    is TypesFilterParams.FilteredRecordTag.Tagged -> {
                        RecordsFilter.Tag.Tagged(it.id)
                    }
                    is TypesFilterParams.FilteredRecordTag.Untagged -> {
                        RecordsFilter.Tag.Untagged(it.typeId)
                    }
                }
            }

            if (filterTags.isNotEmpty()) {
                it + RecordsFilter.FilteredTags(filterTags)
            } else {
                it
            }
        }
    }

    suspend fun getByFilter(filters: List<RecordsFilter>): List<Record> {
        val typeIds: List<Long> = filters.filterIsInstance<RecordsFilter.Activity>()
            .map { it.typeIds }.flatten()
        val comments: List<String> = filters.filterIsInstance<RecordsFilter.Comment>()
            .map { it.comment.lowercase() }
        val ranges: List<Range> = filters.filterIsInstance<RecordsFilter.Date>()
            .map { it.range }
        val selectedTags: List<RecordsFilter.Tag> = filters.filterIsInstance<RecordsFilter.SelectedTags>()
            .map { it.tags }.flatten()
        val selectedTaggedIds: List<Long> = selectedTags.filterIsInstance<RecordsFilter.Tag.Tagged>()
            .map { it.tagId }
        val selectedUntaggedIds: List<Long> = selectedTags.filterIsInstance<RecordsFilter.Tag.Untagged>()
            .map { it.typeId }
        val filteredTags: List<RecordsFilter.Tag> = filters.filterIsInstance<RecordsFilter.FilteredTags>()
            .map { it.tags }.flatten()
        val filteredTaggedIds: List<Long> = filteredTags.filterIsInstance<RecordsFilter.Tag.Tagged>()
            .map { it.tagId }
        val filteredUntaggedIds: List<Long> = filteredTags.filterIsInstance<RecordsFilter.Tag.Untagged>()
            .map { it.typeId }
        val manuallyFilteredIds: List<Long> = filters.filterIsInstance<RecordsFilter.ManuallyFiltered>()
            .map { it.recordIds }.flatten()

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