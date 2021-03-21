package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import javax.inject.Inject

class StatisticsDetailViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val statisticsDetailInteractor: StatisticsDetailInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper
) {

    suspend fun getStatsViewData(
        id: Long,
        filter: ChartFilterType
    ): StatisticsDetailStatsViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        if (id == -1L) return statisticsDetailViewDataMapper.map(
            records = emptyList(),
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime
        )

        val records = when (filter) {
            ChartFilterType.ACTIVITY -> {
                recordInteractor.getByType(typeIds = listOf(id))
            }
            ChartFilterType.CATEGORY -> {
                val types = recordTypeCategoryInteractor.getTypes(categoryId = id)
                recordInteractor.getByType(types)
            }
        }

        return statisticsDetailViewDataMapper.map(
            records = records,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime
        )
    }

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

    suspend fun getChartViewData(
        id: Long,
        chartGrouping: ChartGrouping,
        chartLength: ChartLength,
        filter: ChartFilterType,
        rangeLength: RangeLength,
        rangePosition: Int
    ): StatisticsDetailChartViewData {
        // If untracked
        if (id == -1L) {
            statisticsDetailViewDataMapper.mapToChartViewData(emptyList())
        }

        val typesIds = when (filter) {
            ChartFilterType.ACTIVITY -> {
                listOf(id)
            }
            ChartFilterType.CATEGORY -> {
                recordTypeCategoryInteractor.getTypes(categoryId = id)
            }
        }
        val data = statisticsDetailInteractor.getChartData(
            typeIds = typesIds,
            grouping = chartGrouping,
            chartLength = chartLength,
            rangeLength = rangeLength,
            rangePosition = rangePosition
        )

        return statisticsDetailViewDataMapper.mapToChartViewData(data)
    }

    suspend fun getDailyChartViewData(
        id: Long,
        filter: ChartFilterType
    ): StatisticsDetailChartViewData {
        // If untracked
        if (id == -1L) {
            statisticsDetailViewDataMapper.mapToDailyChartViewData(emptyMap())
        }

        val typesIds = when (filter) {
            ChartFilterType.ACTIVITY -> {
                listOf(id)
            }
            ChartFilterType.CATEGORY -> {
                recordTypeCategoryInteractor.getTypes(categoryId = id)
            }
        }
        val data = statisticsDetailInteractor.getDailyDurations(
            typeIds = typesIds
        )

        return statisticsDetailViewDataMapper.mapToDailyChartViewData(data)
    }
}