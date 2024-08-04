package com.example.util.simpletimetracker.feature_dialogs.chartFilter.interactor

import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import javax.inject.Inject

class ChartFilterInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun setChartFilterType(
        extra: ChartFilterDialogParams,
        filterType: ChartFilterType,
    ) {
        when (extra.type) {
            is ChartFilterDialogParams.Type.RecordsList -> {
                // Do nothing
                // Only activities available on records list filter.
            }
            is ChartFilterDialogParams.Type.Statistics -> {
                prefsInteractor.setChartFilterType(filterType)
            }
        }
    }

    suspend fun setFilteredTypes(
        extra: ChartFilterDialogParams,
        typeIdsFiltered: List<Long>,
    ) {
        when (extra.type) {
            is ChartFilterDialogParams.Type.RecordsList -> {
                prefsInteractor.setFilteredTypesOnList(typeIdsFiltered)
            }
            is ChartFilterDialogParams.Type.Statistics -> {
                prefsInteractor.setFilteredTypes(typeIdsFiltered)
            }
        }
    }

    suspend fun setFilteredCategories(
        extra: ChartFilterDialogParams,
        categoryIdsFiltered: List<Long>,
    ) {
        when (extra.type) {
            is ChartFilterDialogParams.Type.RecordsList -> {
                // Do nothing
                // Only activities available on records list filter.
            }
            is ChartFilterDialogParams.Type.Statistics -> {
                prefsInteractor.setFilteredCategories(categoryIdsFiltered)
            }
        }
    }

    suspend fun setFilteredTags(
        extra: ChartFilterDialogParams,
        recordTagsFiltered: List<Long>,
    ) {
        when (extra.type) {
            is ChartFilterDialogParams.Type.RecordsList -> {
                // Do nothing
                // Only activities available on records list filter.
            }
            is ChartFilterDialogParams.Type.Statistics -> {
                prefsInteractor.setFilteredTags(recordTagsFiltered)
            }
        }
    }

    suspend fun getChartFilterType(
        extra: ChartFilterDialogParams,
    ): ChartFilterType {
        return when (extra.type) {
            is ChartFilterDialogParams.Type.RecordsList -> {
                // Only activities available on records list filter.
                ChartFilterType.ACTIVITY
            }
            is ChartFilterDialogParams.Type.Statistics -> {
                prefsInteractor.getChartFilterType()
            }
        }
    }

    suspend fun getFilteredTypes(
        extra: ChartFilterDialogParams,
    ): List<Long> {
        return when (extra.type) {
            is ChartFilterDialogParams.Type.RecordsList -> {
                prefsInteractor.getFilteredTypesOnList()
            }
            is ChartFilterDialogParams.Type.Statistics -> {
                prefsInteractor.getFilteredTypes()
            }
        }
    }

    suspend fun getFilteredCategories(
        extra: ChartFilterDialogParams,
    ): List<Long> {
        return when (extra.type) {
            is ChartFilterDialogParams.Type.RecordsList -> {
                // Only activities available on records list filter.
                emptyList()
            }
            is ChartFilterDialogParams.Type.Statistics -> {
                prefsInteractor.getFilteredCategories()
            }
        }
    }

    suspend fun getFilteredTags(
        extra: ChartFilterDialogParams,
    ): List<Long> {
        return when (extra.type) {
            is ChartFilterDialogParams.Type.RecordsList -> {
                // Only activities available on records list filter.
                emptyList()
            }
            is ChartFilterDialogParams.Type.Statistics -> {
                prefsInteractor.getFilteredTags()
            }
        }
    }
}