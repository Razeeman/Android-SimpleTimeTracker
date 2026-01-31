package com.example.util.simpletimetracker.feature_widget.statistics.interactor

import com.example.util.simpletimetracker.core.interactor.StatisticsChartViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_views.pieChart.PiePortion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WidgetStatisticsViewDataInteractor @Inject constructor(
    private val statisticsChartViewDataInteractor: StatisticsChartViewDataInteractor,
    private val widgetStatisticsIdsInteractor: WidgetStatisticsIdsInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val timeMapper: TimeMapper,
) {

    suspend fun getViewData(
        appWidgetId: Int,
    ): Result = withContext(Dispatchers.IO) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val durationFormat = prefsInteractor.getDurationFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val widgetData = prefsInteractor.getStatisticsWidget(appWidgetId)
        val types = recordTypeInteractor.getAll()
        val typesMap = types.associateBy(RecordType::id)
        val filterType = widgetData.chartFilterType
        val rangeLength = widgetData.rangeLength
        val filteredIds = widgetStatisticsIdsInteractor.getActualFilteredIds(
            widgetData = widgetData,
            typeIds = { types.map(RecordType::id).toSet() },
            categoryIds = { categoryInteractor.getAll().map(Category::id).toSet() },
            tagIds = { recordTagInteractor.getAll().map(RecordTag::id).toSet() },
        ).toList()

        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = typesMap,
        )
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = 0,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val statistics = statisticsMediator.getStatistics(
            filterType = filterType,
            filteredIds = filteredIds,
            range = range,
        )
        val chart = statisticsChartViewDataInteractor.getChart(
            filterType = filterType,
            filteredIds = filteredIds,
            statistics = statistics,
            dataHolders = dataHolders,
            types = typesMap,
            isDarkTheme = isDarkTheme,
        )
        val total: String = statisticsMediator.getStatisticsTotalTracked(
            statistics = statistics,
            filteredIds = filteredIds,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
        )

        Result(
            chart = chart,
            total = total,
        )
    }

    data class Result(
        val chart: List<PiePortion>,
        val total: String,
    )
}