package com.example.util.simpletimetracker.feature_running_records.interactor

import com.example.util.simpletimetracker.core.mapper.ActivityFilterViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordViewDataMapper
import javax.inject.Inject

class RunningRecordsViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val mapper: RunningRecordViewDataMapper,
    private val activityFilterViewDataMapper: ActivityFilterViewDataMapper,
) {

    suspend fun getViewData(): List<ViewHolderType> {
        val recordTypes = recordTypeInteractor.getAll()
        val recordTypesMap = recordTypes.associateBy { it.id }
        val recordTags = recordTagInteractor.getAll()
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypesRunning = runningRecords.map { it.id }
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        val runningRecordsViewData = when {
            recordTypes.filterNot { it.hidden }.isEmpty() ->
                listOf(mapper.mapToTypesEmpty())
            runningRecords.isEmpty() ->
                listOf(mapper.mapToEmpty())
            else ->
                runningRecords
                    .sortedByDescending {
                        it.timeStarted
                    }
                    .mapNotNull { runningRecord ->
                        mapper.map(
                            runningRecord = runningRecord,
                            recordType = recordTypesMap[runningRecord.id] ?: return@mapNotNull null,
                            recordTags = recordTags.filter { it.id in runningRecord.tagIds },
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime
                        )
                    }
        }

        val filter = getFilter()
        val filtersViewData = getFilterViewData(
            filter = filter,
            isDarkTheme = isDarkTheme
        )

        val recordTypesViewData = recordTypes
            .filterNot {
                it.hidden
            }
            .let { list ->
                applyFilter(list, filter)
            }
            .map {
                mapper.map(
                    recordType = it,
                    isFiltered = it.id in recordTypesRunning,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme
                )
            }
            .plus(
                mapper.mapToAddItem(
                    numberOfCards,
                    isDarkTheme
                )
            )

        return runningRecordsViewData +
            listOf(DividerViewData(1)) +
            filtersViewData +
            recordTypesViewData
    }

    private suspend fun getFilter(): Filter {
        return if (prefsInteractor.getShowActivityFilters()) {
            val activityFilters = activityFilterInteractor.getAll()
            Filter.ApplyFilter(activityFilters)
        } else {
            Filter.NoFilter
        }
    }

    private fun getFilterViewData(
        filter: Filter,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        return when (filter) {
            is Filter.NoFilter -> emptyList()
            is Filter.ApplyFilter -> filter.activityFilters
                .map {
                    activityFilterViewDataMapper.map(
                        filter = it,
                        isDarkTheme = isDarkTheme,
                    )
                }
                .plus(
                    mapper.mapToActivityFilterAddItem(
                        isDarkTheme = isDarkTheme,
                    )
                )
                .plus(
                    DividerViewData(2)
                )
        }
    }

    private suspend fun applyFilter(
        list: List<RecordType>,
        filter: Filter,
    ): List<RecordType> {
        return if (filter is Filter.ApplyFilter && filter.activityFilters.any { it.selected }) {
            val selectedTypes = getSelectedTypeIds(filter.activityFilters)
            list.filter { it.id in selectedTypes }
        } else {
            list
        }
    }

    private suspend fun getSelectedTypeIds(filters: List<ActivityFilter>): List<Long> {
        val selectedFilters = filters.filter { it.selected }

        if (selectedFilters.isEmpty()) return emptyList()

        val activityIds: List<Long> = selectedFilters
            .filter { it.type is ActivityFilter.Type.Activity }
            .map { it.selectedIds }
            .flatten()

        val fromCategoryIds: List<Long> = selectedFilters
            .filter { it.type is ActivityFilter.Type.Category }
            .map { it.selectedIds }
            .flatten()
            .takeUnless { it.isEmpty() }
            ?.let { tagIds ->
                val recordTypeCategories = recordTypeCategoryInteractor.getAll()
                    .groupBy { it.categoryId }
                    .mapValues { (_, value) -> value.map { it.recordTypeId } }
                tagIds.mapNotNull { tagId -> recordTypeCategories[tagId] }.flatten()
            }
            .orEmpty()

        return when {
            fromCategoryIds.isEmpty() -> activityIds
            activityIds.isEmpty() -> fromCategoryIds
            else -> activityIds + fromCategoryIds
        }
    }

    private sealed interface Filter {
        object NoFilter : Filter
        data class ApplyFilter(
            val activityFilters: List<ActivityFilter>,
        ) : Filter
    }
}