package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.recordType.extension.getDaily
import com.example.util.simpletimetracker.domain.recordType.extension.getMonthly
import com.example.util.simpletimetracker.domain.recordType.extension.getWeekly
import com.example.util.simpletimetracker.domain.recordType.extension.getYearly
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
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailButtonViewData
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
    private val resourceRepo: ResourceRepo,
) {

    fun getSelectedGoal(
        goals: List<RecordTypeGoal>,
        goalPosition: Int?,
    ): RecordTypeGoal? {
        return goalPosition?.let { goals.getOrNull(it) } ?: goals.getLongest()
    }

    suspend fun getGoalsForRange(
        filter: List<RecordsFilter>,
        chartGrouping: ChartGrouping,
    ): List<RecordTypeGoal> {
        return getGoalsForRange(
            goals = statisticsDetailGetGoalFromFilterInteractor.execute(filter),
            rangeLength = mapToRange(chartGrouping)
        )
    }

    // TODO compare?
    suspend fun getChartViewData(
        records: List<RecordBase>,
        filter: List<RecordsFilter>,
        currentChartGrouping: ChartGrouping,
        currentChartLength: ChartLength,
        rangeLength: RangeLength,
        rangePosition: Int,
        goalPosition: Int?,
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
        val chartGoals = getGoalsForRange(
            goals = goals,
            rangeLength = mapToRange(compositeData.appliedChartGrouping),
        )
        val chartGoal = getSelectedGoal(chartGoals, goalPosition)
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
            // TODO GOAL select goal
            // TODO GOAL use the same goal selection everywhere
            currentRangeGoal = getLongestGoal(
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
        val goalSelectViewData = mapGoalSelectViewData(
            goals = chartGoals,
            selectedGoal = chartGoal,
            isDarkTheme = isDarkTheme,
        )

        return@withContext StatisticsDetailGoalsCompositeViewData(
            viewData = statsViewData + goalSelectViewData + chartViewData,
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
            .filter { getLongestGoal(goals, mapToRange(it)).value != 0L }
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
            ChartGrouping.YEARLY -> RangeLength.Year
        }
    }

    private fun mapGoalSelectViewData(
        goals: List<RecordTypeGoal>,
        selectedGoal: RecordTypeGoal?,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        if (goals.size <= 1) return emptyList()
        selectedGoal ?: return emptyList()

        return StatisticsDetailButtonViewData(
            marginTopDp = 10,
            data = StatisticsDetailButtonViewData.Button(
                block = StatisticsDetailBlock.GoalSelect,
                text = statisticsDetailGoalsViewDataMapper.mapGoalName(selectedGoal),
                color = resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme),
            ),
            dataSecond = null,
        ).let(::listOf)
    }

    private fun getLongestGoal(
        goals: List<RecordTypeGoal>,
        rangeLength: RangeLength?,
    ): RecordTypeGoal? {
        return getGoalsForRange(goals, rangeLength).getLongest()
    }

    private fun getGoalsForRange(
        goals: List<RecordTypeGoal>,
        rangeLength: RangeLength?,
    ): List<RecordTypeGoal> {
        return when (rangeLength) {
            is RangeLength.Day -> goals.getDaily()
            is RangeLength.Week -> goals.getWeekly()
            is RangeLength.Month -> goals.getMonthly()
            is RangeLength.Year -> goals.getYearly()
            else -> emptyList()
        }
    }
}