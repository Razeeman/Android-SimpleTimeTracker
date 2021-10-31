package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Statistics
import javax.inject.Inject

class StatisticsMediator @Inject constructor(
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
) {

    suspend fun getAll(
        filterType: ChartFilterType,
    ): List<Statistics> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> statisticsInteractor.getAll()
            ChartFilterType.CATEGORY -> statisticsCategoryInteractor.getAll()
        }
    }

    suspend fun getFromRange(
        filterType: ChartFilterType,
        start: Long,
        end: Long,
        addUntracked: Boolean,
    ): List<Statistics> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> statisticsInteractor.getFromRange(start, end, addUntracked)
            ChartFilterType.CATEGORY -> statisticsCategoryInteractor.getFromRange(start, end)
        }
    }
}