package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordType.extension.isReached
import com.example.util.simpletimetracker.domain.recordType.extension.value
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import java.util.Calendar
import javax.inject.Inject

class StatisticsDetailGoalsViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
) {

    fun mapGoalStatsViewData(
        records: List<RecordBase>,
        currentRangeGoal: RecordTypeGoal?,
        rangeLength: RangeLength,
        rangePosition: Int,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): List<ViewHolderType> {
        val goalValue = getGoalValue(currentRangeGoal)
        val goalSubtype = currentRangeGoal?.subtype ?: RecordTypeGoal.Subtype.Goal
        val goalRange = currentRangeGoal?.range ?: RecordTypeGoal.Range.Daily
        if (goalValue == 0L) return emptyList()

        val items = mutableListOf<ViewHolderType>()
        val chartMode = statisticsDetailViewDataMapper.mapToChartMode(currentRangeGoal)
        val goalStats = mapGoalStats(
            records = records,
            goalValue = goalValue,
            goalRange = goalRange,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            chartMode = chartMode,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )

        if (goalStats.isNotEmpty()) {
            val title = when (goalSubtype) {
                is RecordTypeGoal.Subtype.Goal -> R.string.change_record_type_goal_time_hint
                is RecordTypeGoal.Subtype.Limit -> R.string.change_record_type_limit_time_hint
            }.let(resourceRepo::getString)
            items += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.GoalStats,
                title = title,
                marginTopDp = 10,
                data = goalStats,
            )
        }

        return items
    }

    fun mapGoalChartViewData(
        data: List<ChartBarDataDuration>,
        prevData: List<ChartBarDataDuration>,
        chartGoal: RecordTypeGoal?,
        rangeLength: RangeLength,
        availableChartGroupings: List<ChartGrouping>,
        appliedChartGrouping: ChartGrouping,
        availableChartLengths: List<ChartLength>,
        appliedChartLength: ChartLength,
        chartMode: ChartMode,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
        isDarkTheme: Boolean,
        startOfDayShift: Long,
    ): List<ViewHolderType> {
        val goalValue = getGoalValue(chartGoal)
        if (goalValue == 0L) return emptyList()
        val goalRange = chartGoal?.range ?: return emptyList()
        val goalSubtype = chartGoal.subtype
        val goalDaysOfWeek = chartGoal.daysOfWeek

        val items = mutableListOf<ViewHolderType>()
        val goalData = mapGoalData(
            data = data,
            goalValue = goalValue,
            goalRange = goalRange,
            goalDaysOfWeek = goalDaysOfWeek,
            goalSubtype = goalSubtype,
            isDarkTheme = isDarkTheme,
            startOfDayShift = startOfDayShift,
        )
        val goalChartPrevData = mapGoalData(
            data = prevData,
            goalValue = goalValue,
            goalRange = goalRange,
            goalDaysOfWeek = goalDaysOfWeek,
            goalSubtype = goalSubtype,
            isDarkTheme = isDarkTheme,
            startOfDayShift = startOfDayShift,
        )
        val chartData = statisticsDetailViewDataMapper.mapChartData(
            data = goalData,
            goal = 0, // Don't show goal on goal graph.
            rangeLength = rangeLength,
            chartMode = chartMode,
            yAxisZoomed = false,
            showSelectedBarOnStart = true,
            useSingleColor = false,
            drawRoundCaps = true,
        )
        val (title, rangeAverages) = statisticsDetailViewDataMapper.getRangeAverages(
            data = goalData,
            prevData = goalChartPrevData,
            compareData = emptyList(),
            showComparison = false,
            rangeLength = rangeLength,
            chartGrouping = appliedChartGrouping,
            chartMode = chartMode,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        )
        val chartGroupingViewData = statisticsDetailViewDataMapper.mapToChartGroupingViewData(
            availableChartGroupings = availableChartGroupings,
            appliedChartGrouping = appliedChartGrouping,
        )
        val chartLengthViewData = statisticsDetailViewDataMapper.mapToChartLengthViewData(
            availableChartLengths = availableChartLengths,
            appliedChartLength = appliedChartLength,
        )
        val goalTotals = mapGoalExcessDeficitTotals(
            goalData = goalData,
            chartMode = chartMode,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
        )

        if (chartData.visible) {
            items += StatisticsDetailHintViewData(
                block = StatisticsDetailBlock.GoalExcessDeficitHint,
                text = resourceRepo.getString(R.string.statistics_detail_goals_hint),
            )
        }

        if (chartData.visible) {
            items += StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.GoalChartData,
                singleColor = null, // Replaced later.
                marginTopDp = 0,
                data = chartData,
            )
        }

        if (chartGroupingViewData.size > 1) {
            items += ButtonsRowItemViewData(
                block = StatisticsDetailBlock.GoalChartGrouping,
                marginTopDp = 4,
                data = chartGroupingViewData,
            )
        }

        if (chartLengthViewData.isNotEmpty()) {
            // Update margin top depending if has buttons before.
            val hasButtonsBefore = items.lastOrNull() is ButtonsRowItemViewData
            val marginTopDp = if (hasButtonsBefore) -10 else 4
            items += ButtonsRowItemViewData(
                block = StatisticsDetailBlock.GoalChartLength,
                marginTopDp = marginTopDp,
                data = chartLengthViewData,
            )
        }

        if (rangeAverages.isNotEmpty()) {
            items += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.GoalRangeAverages,
                title = title,
                marginTopDp = 0,
                data = rangeAverages,
            )
        }

        if (chartData.visible) {
            items += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.GoalTotals,
                title = "",
                marginTopDp = 0,
                data = goalTotals,
            )
        }

        return items
    }

    private fun mapGoalExcessDeficitTotals(
        goalData: List<ChartBarDataDuration>,
        chartMode: ChartMode,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): List<StatisticsDetailCardInternalViewData> {
        val barValues = goalData.map { bar -> bar.totalDuration.orZero() }
        val negativeValue = barValues.filter { it < 0L }.sum()
        val positiveValue = barValues.filter { it > 0L }.sum()
        val total = negativeValue + positiveValue

        fun formatInterval(
            interval: Long,
        ): String {
            return when (chartMode) {
                is ChartMode.DURATIONS -> timeMapper.formatInterval(
                    interval = interval,
                    forceSeconds = showSeconds,
                    durationFormat = durationFormat,
                )
                is ChartMode.COUNTS, is ChartMode.TAG_VALUE -> interval.toString()
            }
        }

        return listOf(
            StatisticsDetailCardInternalViewData(
                value = formatInterval(negativeValue),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_goals_deficit),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = formatInterval(total),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_total_duration),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = formatInterval(positiveValue),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_goals_excess),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
        )
    }

    private fun mapGoalData(
        data: List<ChartBarDataDuration>,
        goalValue: Long,
        goalSubtype: RecordTypeGoal.Subtype,
        goalRange: RecordTypeGoal.Range,
        goalDaysOfWeek: Set<DayOfWeek>,
        isDarkTheme: Boolean,
        startOfDayShift: Long,
    ): List<ChartBarDataDuration> {
        if (goalValue == 0L) return emptyList()
        val shouldAccountForDays = goalRange is RecordTypeGoal.Range.Daily
        val greenColor = resourceRepo.getThemedAttr(R.attr.appPositiveColor, isDarkTheme)
        val redColor = resourceRepo.getThemedAttr(R.attr.appNegativeColor, isDarkTheme)
        val positiveColor = when (goalSubtype) {
            is RecordTypeGoal.Subtype.Goal -> greenColor
            is RecordTypeGoal.Subtype.Limit -> redColor
        }
        val negativeColor = when (goalSubtype) {
            is RecordTypeGoal.Subtype.Goal -> redColor
            is RecordTypeGoal.Subtype.Limit -> greenColor
        }
        val calendar = Calendar.getInstance()
        val current = System.currentTimeMillis()

        return data.map { dataPart ->
            val totalDuration = dataPart.totalDuration.orZero()
            val goalDuration = if (dataPart.rangeStart > current) {
                0
            } else if (shouldAccountForDays) {
                val currentPartDay = timeMapper.getDayOfWeek(
                    timestamp = dataPart.rangeStart,
                    calendar = calendar,
                    startOfDayShift = startOfDayShift,
                )
                if (currentPartDay in goalDaysOfWeek) {
                    totalDuration - goalValue
                } else {
                    0
                }
            } else {
                totalDuration - goalValue
            }
            val isReached = goalSubtype.isReached(
                current = goalDuration,
                goalValue = 0L,
            )
            val color = if (isReached) positiveColor else negativeColor
            ChartBarDataDuration(
                rangeStart = dataPart.rangeStart,
                legend = dataPart.legend,
                durations = listOf(goalDuration to color),
            )
        }
    }

    private fun mapGoalStats(
        records: List<RecordBase>,
        goalValue: Long,
        goalRange: RecordTypeGoal.Range,
        rangeLength: RangeLength,
        rangePosition: Int,
        chartMode: ChartMode,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): List<StatisticsDetailCardInternalViewData> {
        fun formatInterval(
            interval: Long,
        ): String {
            return when (chartMode) {
                is ChartMode.DURATIONS -> timeMapper.formatInterval(
                    interval = interval,
                    forceSeconds = showSeconds,
                    durationFormat = durationFormat,
                )
                is ChartMode.COUNTS, is ChartMode.TAG_VALUE -> interval.toString()
            }
        }

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val recordsFromRange = if (range.isUndefined) {
            records
        } else {
            rangeMapper.getRecordsFromRange(records, range)
                .map { rangeMapper.clampRecordToRange(it, range) }
        }
        val currentValue = when (chartMode) {
            is ChartMode.DURATIONS -> recordsFromRange.sumOf(RecordBase::duration)
            is ChartMode.COUNTS, is ChartMode.TAG_VALUE -> recordsFromRange.size.toLong()
        }
        val percentage = if (goalValue != 0L) {
            currentValue * 100f / goalValue
        } else {
            0f
        }
        val percentageString = statisticsDetailViewDataMapper.processPercentageString(percentage)
        val description = when (goalRange) {
            is RecordTypeGoal.Range.Session -> 0 // Shouldn't be possible.
            is RecordTypeGoal.Range.Daily -> R.string.range_day
            is RecordTypeGoal.Range.Weekly -> R.string.range_week
            is RecordTypeGoal.Range.Monthly -> R.string.range_month
        }.let(resourceRepo::getString)

        return listOf(
            StatisticsDetailCardInternalViewData(
                value = formatInterval(goalValue),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = description,
            ),
            StatisticsDetailCardInternalViewData(
                value = formatInterval(currentValue),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = percentageString,
            ),
        )
    }

    private fun getGoalValue(
        goal: RecordTypeGoal?,
    ): Long {
        return when (goal?.type) {
            is RecordTypeGoal.Type.Duration -> goal.value * 1000
            is RecordTypeGoal.Type.Count -> goal.value
            null -> 0L
        }
    }
}