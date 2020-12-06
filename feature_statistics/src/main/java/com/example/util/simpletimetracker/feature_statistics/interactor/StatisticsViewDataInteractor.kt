package com.example.util.simpletimetracker.feature_statistics.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.domain.model.StatisticsCategory
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.feature_statistics.viewData.RangeLength
import java.util.Calendar
import javax.inject.Inject

class StatisticsViewDataInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsViewDataMapper: StatisticsViewDataMapper
) {

    suspend fun getViewData(rangeLength: RangeLength?, shift: Int): List<ViewHolderType> {
        val filterType = prefsInteractor.getChartFilterType()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val (start, end) = getRange(rangeLength, shift)
        val showDuration = start.orZero() != 0L && end.orZero() != 0L

        val list: List<ViewHolderType>
        val totalTracked: ViewHolderType
        val chart: ViewHolderType

        when (filterType) {
            ChartFilterType.ACTIVITY -> {
                val types = recordTypeInteractor.getAll()
                val typesFiltered = prefsInteractor.getFilteredTypes()
                val statistics = getStatistics(rangeLength, shift, typesFiltered)

                list = statisticsViewDataMapper.mapActivities(
                    statistics, types, typesFiltered, showDuration, isDarkTheme
                )
                chart = statisticsViewDataMapper.mapActivitiesToChart(
                    statistics, types, typesFiltered, isDarkTheme
                )
                totalTracked = statisticsViewDataMapper.mapActivitiesTotalTracked(
                    statistics, typesFiltered
                )
            }
            ChartFilterType.CATEGORY -> {
                val categories = categoryInteractor.getAll()
                val categoriesFiltered = prefsInteractor.getFilteredCategories()
                val statistics = getStatisticsCategory(rangeLength, shift)

                list = statisticsViewDataMapper.mapCategories(
                    statistics, categories, categoriesFiltered, showDuration, isDarkTheme
                )
                chart = statisticsViewDataMapper.mapCategoriesToChart(
                    statistics, categories, categoriesFiltered, isDarkTheme
                )
                totalTracked = statisticsViewDataMapper.mapCategoriesTotalTracked(
                    statistics, categoriesFiltered
                )
            }
        }

        if (list.isEmpty()) return listOf(statisticsViewDataMapper.mapToEmpty())
        return listOf(chart) + list + totalTracked
    }

    private suspend fun getStatistics(
        rangeLength: RangeLength?,
        shift: Int,
        typesFiltered: List<Long>
    ): List<Statistics> {
        val (start, end) = getRange(rangeLength, shift)

        return if (start.orZero() != 0L && end.orZero() != 0L) {
            statisticsInteractor.getFromRange(
                start = start.orZero(),
                end = end.orZero(),
                addUntracked = !typesFiltered.contains(-1L)
            )
        } else {
            statisticsInteractor.getAll()
        }
    }

    private suspend fun getStatisticsCategory(
        rangeLength: RangeLength?,
        shift: Int
    ): List<StatisticsCategory> {
        val (start, end) = getRange(rangeLength, shift)

        return if (start.orZero() != 0L && end.orZero() != 0L) {
            statisticsCategoryInteractor.getFromRange(
                start = start.orZero(),
                end = end.orZero()
            )
        } else {
            statisticsCategoryInteractor.getAll()
        }
    }

    private fun getRange(rangeLength: RangeLength?, shift: Int): Pair<Long, Long> {
        val rangeStart: Long
        val rangeEnd: Long
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (rangeLength) {
            RangeLength.DAY -> {
                calendar.add(Calendar.DATE, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis
            }
            RangeLength.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.add(Calendar.DATE, shift * 7)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 7) }.timeInMillis
            }
            RangeLength.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis
            }
            RangeLength.ALL -> {
                rangeStart = 0L
                rangeEnd = 0L
            }
            else -> {
                rangeStart = 0L
                rangeEnd = 0L
            }
        }

        return rangeStart to rangeEnd
    }
}