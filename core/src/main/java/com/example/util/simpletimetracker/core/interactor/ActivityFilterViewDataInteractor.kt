package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.ActivityFilterViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import javax.inject.Inject

class ActivityFilterViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val activityFilterViewDataMapper: ActivityFilterViewDataMapper,
) {

    suspend fun getFilter(): Filter {
        return if (prefsInteractor.getShowActivityFilters()) {
            val activityFilters = activityFilterInteractor.getAll()
            Filter.ApplyFilter(activityFilters)
        } else {
            Filter.NoFilter
        }
    }

    fun getFilterViewData(
        filter: Filter,
        isDarkTheme: Boolean,
        appendAddButton: Boolean,
    ): List<ViewHolderType> {
        return when (filter) {
            is Filter.NoFilter -> {
                emptyList()
            }
            is Filter.ApplyFilter -> {
                filter.activityFilters
                    .map {
                        activityFilterViewDataMapper.map(
                            filter = it,
                            isDarkTheme = isDarkTheme,
                        )
                    }
                    .let {
                        if (appendAddButton) {
                            it + activityFilterViewDataMapper.mapToActivityFilterAddItem(
                                isDarkTheme = isDarkTheme
                            )
                        } else {
                            it
                        }
                    }
            }
        }
    }

    suspend fun applyFilter(
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

    sealed interface Filter {
        object NoFilter : Filter
        data class ApplyFilter(
            val activityFilters: List<ActivityFilter>,
        ) : Filter
    }
}