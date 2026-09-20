package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.domain.recordType.extension.getDaily
import com.example.util.simpletimetracker.domain.recordType.extension.getMonthly
import com.example.util.simpletimetracker.domain.recordType.extension.getWeekly
import com.example.util.simpletimetracker.domain.recordType.extension.value
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.recordType.extension.getLongest
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailChartInteractor.CompositeChartData
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailGoalsViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartSplitSortMode
import com.example.util.simpletimetracker.domain.statistics.model.ChartValueMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGoalsCompositeViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsDetailGoalsInteractor @Inject constructor(
    private val chartInteractor: StatisticsDetailChartInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val statisticsDetailGoalsViewDataMapper: StatisticsDetailGoalsViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsDetailGetGoalFromFilterInteractor: StatisticsDetailGetGoalFromFilterInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
) {

    // TODO compare?
    suspend fun getChartViewData(
        records: List<RecordBase>,
        filter: List<RecordsFilter>,
        currentChartGrouping: ChartGrouping,
        currentChartLength: ChartLength,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): StatisticsDetailGoalsCompositeViewData = withContext(Dispatchers.Default) {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val durationFormat = prefsInteractor.getDurationFormat()
        val useMonthDayTimeFormat = prefsInteractor.getUseMonthDayTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val types = recordTypeInteractor.getAll()
        val typesMap = types.associateBy(RecordType::id)
        val typesOrder = types.map(RecordType::id)
        val goals = statisticsDetailGetGoalFromFilterInteractor.execute(filter)

        val compositeData = getChartRangeSelectionData(
            currentChartGrouping = currentChartGrouping,
            currentChartLength = currentChartLength,
            rangeLength = rangeLength,
            firstDayOfWeek = firstDayOfWeek,
            goals = goals,
        )
        val chartGoal = getGoal(
            goals = goals,
            rangeLength = mapToRange(compositeData.appliedChartGrouping),
        )
        val chartMode = statisticsDetailViewDataMapper.mapToChartMode(chartGoal)
        val chartValueMode = ChartValueMode.TOTAL
        val ranges = chartInteractor.getRanges(
            compositeData = compositeData,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            useMonthDayTimeFormat = useMonthDayTimeFormat,
        )
        val data = chartInteractor.getChartData(
            allRecords = records,
            ranges = ranges,
            typesOrder = typesOrder,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            chartMode = chartMode,
            chartValueMode = chartValueMode,
            multiplyDuration = false,
            splitByActivity = false,
            splitSortMode = ChartSplitSortMode.ACTIVITY_ORDER,
        )
        val prevData = chartInteractor.getPrevData(
            rangeLength = rangeLength,
            compositeData = compositeData,
            rangePosition = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            useMonthDayTimeFormat = useMonthDayTimeFormat,
            records = records,
            typesOrder = typesOrder,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            chartMode = chartMode,
            chartValueMode = chartValueMode,
            multiplyDuration = false,
            splitSortMode = ChartSplitSortMode.ACTIVITY_ORDER,
        )

        val statsViewData = statisticsDetailGoalsViewDataMapper.mapGoalStatsViewData(
            records = records,
            currentRangeGoal = getGoal(
                goals = goals,
                rangeLength = rangeLength,
            ),
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val chartViewData = statisticsDetailGoalsViewDataMapper.mapGoalChartViewData(
            data = data,
            prevData = prevData,
            chartGoal = chartGoal,
            rangeLength = rangeLength,
            availableChartGroupings = compositeData.availableChartGroupings,
            appliedChartGrouping = compositeData.appliedChartGrouping,
            availableChartLengths = compositeData.availableChartLengths,
            appliedChartLength = compositeData.appliedChartLength,
            chartMode = chartMode,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
            startOfDayShift = startOfDayShift,
        )

        return@withContext StatisticsDetailGoalsCompositeViewData(
            viewData = statsViewData + chartViewData,
            appliedChartGrouping = compositeData.appliedChartGrouping,
            appliedChartLength = compositeData.appliedChartLength,
        )
    }

    private fun getChartRangeSelectionData(
        currentChartGrouping: ChartGrouping,
        currentChartLength: ChartLength,
        rangeLength: RangeLength,
        firstDayOfWeek: DayOfWeek,
        goals: List<RecordTypeGoal>,
    ): CompositeChartData {
        val mainData = chartInteractor.getChartRangeSelectionData(
            currentChartGrouping = currentChartGrouping,
            currentChartLength = currentChartLength,
            rangeLength = rangeLength,
            firstDayOfWeek = firstDayOfWeek,
        )

        val availableChartGroupings = mainData.availableChartGroupings
            .filter { getGoal(goals, mapToRange(it)).value != 0L }
            .takeUnless { it.isEmpty() }
            ?: listOf(ChartGrouping.DAILY)

        return mainData.copy(
            availableChartGroupings = availableChartGroupings,
            appliedChartGrouping = mainData.appliedChartGrouping
                .takeIf { it in availableChartGroupings }
                ?: availableChartGroupings.firstOrNull()
                ?: ChartGrouping.DAILY,
        )
    }

    private fun mapToRange(
        appliedChartGrouping: ChartGrouping,
    ): RangeLength? {
        return when (appliedChartGrouping) {
            ChartGrouping.DAILY -> RangeLength.Day
            ChartGrouping.WEEKLY -> RangeLength.Week
            ChartGrouping.MONTHLY -> RangeLength.Month
            ChartGrouping.YEARLY -> null
        }
    }

    // TODO GOALS show several goals on chart
    // TODO GOALS show several goals on excess / deficit
    private fun getGoal(
        goals: List<RecordTypeGoal>,
        rangeLength: RangeLength?,
    ): RecordTypeGoal? {
        return when (rangeLength) {
            is RangeLength.Day -> goals.getDaily()
            is RangeLength.Week -> goals.getWeekly()
            is RangeLength.Month -> goals.getMonthly()
            else -> null
        }?.getLongest()
    }
}