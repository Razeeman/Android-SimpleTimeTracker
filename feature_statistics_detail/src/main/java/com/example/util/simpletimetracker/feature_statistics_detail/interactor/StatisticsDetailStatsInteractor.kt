package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import javax.inject.Inject

class StatisticsDetailStatsInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val recordInteractor: RecordInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val timeMapper: TimeMapper
) {

    suspend fun getStatsViewData(
        id: Long,
        filter: ChartFilterType,
        rangeLength: RangeLength,
        rangePosition: Int
    ): StatisticsDetailStatsViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        if (id == -1L) return statisticsDetailViewDataMapper.mapStatsData(
            records = emptyList(),
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime
        )

        val range = timeMapper.getRangeStartAndEnd(rangeLength, rangePosition)
        val types = when (filter) {
            ChartFilterType.ACTIVITY -> {
                listOf(id)
            }
            ChartFilterType.CATEGORY -> {
                recordTypeCategoryInteractor.getTypes(categoryId = id)
            }
        }
        val records = if (range.first == 0L && range.second == 0L) {
            recordInteractor.getAll()
        } else {
            recordInteractor.getFromRange(range.first, range.second)
        }.filter {
            // Skip records that started before this time range.
            it.typeId in types && it.timeStarted > range.first
        }

        return statisticsDetailViewDataMapper.mapStatsData(
            records = records,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime
        )
    }
}