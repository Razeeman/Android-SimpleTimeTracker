package com.example.util.simpletimetracker.feature_statistics.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.domain.model.StatisticsCategory
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.core.adapter.statistics.StatisticsViewData
import javax.inject.Inject

class StatisticsViewDataInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsViewDataMapper: StatisticsViewDataMapper,
    private val timeMapper: TimeMapper
) {

    suspend fun getViewData(rangeLength: RangeLength, shift: Int): List<ViewHolderType> {
        val filterType = prefsInteractor.getChartFilterType()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val showDuration = rangeLength != RangeLength.ALL

        val list: List<StatisticsViewData>
        val totalTracked: ViewHolderType
        val chart: ViewHolderType
        val result: MutableList<ViewHolderType> = mutableListOf()

        when (filterType) {
            ChartFilterType.ACTIVITY -> {
                val types = recordTypeInteractor.getAll()
                val typesFiltered = prefsInteractor.getFilteredTypes()
                val statistics = getStatistics(rangeLength, shift, typesFiltered, firstDayOfWeek)

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
                val types = recordTypeInteractor.getAll()
                val typeCategories = recordTypeCategoryInteractor.getAll()
                val categoriesFiltered = prefsInteractor.getFilteredCategories()
                val statistics = getStatisticsCategory(rangeLength, shift, firstDayOfWeek)

                list = statisticsViewDataMapper.mapCategories(
                    statistics, categories, categoriesFiltered, showDuration, isDarkTheme
                )
                chart = statisticsViewDataMapper.mapCategoriesToChart(
                    statistics, categories, types, typeCategories, categoriesFiltered, isDarkTheme
                )
                totalTracked = statisticsViewDataMapper.mapCategoriesTotalTracked(
                    statistics, categoriesFiltered
                )
            }
        }
        val hint = statisticsViewDataMapper.mapToHint()

        if (list.isEmpty()) {
            statisticsViewDataMapper.mapToEmpty().let(result::add)
        } else {
            chart.let(result::add)
            list.let(result::addAll)
            totalTracked.let(result::add)
            // If has any activity or tag other than untracked
            if (list.any { it.id != -1L }) hint.let(result::add)
        }

        return result
    }

    private suspend fun getStatistics(
        rangeLength: RangeLength,
        shift: Int,
        typesFiltered: List<Long>,
        firstDayOfWeek: DayOfWeek
    ): List<Statistics> {
        val (start, end) = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = shift,
            firstDayOfWeek = firstDayOfWeek
        )

        return if (start != 0L && end != 0L) {
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
        rangeLength: RangeLength,
        shift: Int,
        firstDayOfWeek: DayOfWeek
    ): List<StatisticsCategory> {
        val (start, end) = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = shift,
            firstDayOfWeek = firstDayOfWeek
        )

        return if (start != 0L && end != 0L) {
            statisticsCategoryInteractor.getFromRange(
                start = start.orZero(),
                end = end.orZero()
            )
        } else {
            statisticsCategoryInteractor.getAll()
        }
    }
}