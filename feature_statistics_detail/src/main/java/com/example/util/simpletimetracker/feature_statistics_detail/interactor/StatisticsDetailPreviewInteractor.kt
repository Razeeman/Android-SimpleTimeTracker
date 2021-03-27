package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import javax.inject.Inject

class StatisticsDetailPreviewInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper
) {

    suspend fun getPreviewData(
        id: Long,
        filter: ChartFilterType
    ): StatisticsDetailPreviewViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        if (id == -1L) return statisticsDetailViewDataMapper.mapToPreviewUntracked(isDarkTheme)

        val name: String?
        val color: Int?
        val icon: String?

        when (filter) {
            ChartFilterType.ACTIVITY -> {
                val recordType = recordTypeInteractor.get(id)
                name = recordType?.name
                color = recordType?.color
                icon = recordType?.icon
            }
            ChartFilterType.CATEGORY -> {
                val category = categoryInteractor.get(id)
                name = category?.name
                color = category?.color
                icon = null
            }
        }

        return statisticsDetailViewDataMapper.mapToPreview(name, icon, color, isDarkTheme)
    }
}