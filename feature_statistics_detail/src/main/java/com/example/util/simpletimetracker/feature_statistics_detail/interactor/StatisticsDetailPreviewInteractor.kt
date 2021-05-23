package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.navigation.params.TypesFilterParams
import javax.inject.Inject

class StatisticsDetailPreviewInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper
) {

    suspend fun getPreviewData(
        filterParams: TypesFilterParams
    ): List<StatisticsDetailPreviewViewData> {
        val selectedIds = filterParams.selectedIds
        val filter = filterParams.filterType
        val isDarkTheme = prefsInteractor.getDarkMode()

        val viewData = when (filter) {
            ChartFilterType.ACTIVITY -> {
                recordTypeInteractor.getAll()
                    .filter { it.id in selectedIds }
                    .mapIndexed { index, type ->
                        statisticsDetailViewDataMapper.mapToPreview(
                            recordType = type,
                            isDarkTheme = isDarkTheme,
                            isFirst = index == 0
                        )
                    }
            }
            ChartFilterType.CATEGORY -> {
                categoryInteractor.getAll()
                    .filter { it.id in selectedIds }
                    .map { category ->
                        statisticsDetailViewDataMapper.mapToPreview(
                            category = category,
                            isDarkTheme = isDarkTheme
                        )
                    }
            }
        }

        return viewData
            .takeUnless { it.isEmpty() }
            ?: statisticsDetailViewDataMapper
                .mapToPreviewEmpty(isDarkTheme)
                .let(::listOf)
    }
}