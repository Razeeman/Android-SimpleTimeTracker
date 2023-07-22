package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.StatisticsGoalViewData
import javax.inject.Inject
import kotlin.math.roundToLong

class GoalViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val timeMapper: TimeMapper,
) {

    fun mapList(
        rangeLength: RangeLength,
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<StatisticsGoalViewData> {
        return statistics
            .mapNotNull { statistic ->
                mapItem(
                    rangeLength = rangeLength,
                    statistics = statistic,
                    dataHolder = data[statistic.id] ?: return@mapNotNull null,
                    isDarkTheme = isDarkTheme,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                )
            }
            .sortedBy { it.goal.percent }
    }

    fun mapItem(
        rangeLength: RangeLength,
        statistics: Statistics,
        dataHolder: StatisticsDataHolder,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): StatisticsGoalViewData {
        return StatisticsGoalViewData(
            id = statistics.id,
            name = dataHolder.name,
            icon = dataHolder.icon.orEmpty()
                .let(iconMapper::mapIcon),
            color = dataHolder.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
            goal = mapGoal(
                statistics = statistics,
                dataHolder = dataHolder,
                rangeLength = rangeLength,
                useProportionalMinutes = useProportionalMinutes,
                showSeconds = showSeconds,
            ),
        )
    }

    private fun mapGoal(
        statistics: Statistics,
        dataHolder: StatisticsDataHolder,
        rangeLength: RangeLength,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): StatisticsGoalViewData.Goal {
        val goal = when (rangeLength) {
            is RangeLength.Day -> dataHolder.dailyGoalTime
            is RangeLength.Week -> dataHolder.weeklyGoalTime
            is RangeLength.Month -> dataHolder.monthlyGoalTime
            else -> return StatisticsGoalViewData.Goal.empty()
        } * 1000

        if (goal == 0L) return StatisticsGoalViewData.Goal.empty()

        val current = statistics.duration
        val goalComplete = goal - current <= 0L
        val currentString = timeMapper.formatInterval(
            interval = current,
            forceSeconds = showSeconds,
            useProportionalMinutes = useProportionalMinutes,
        )
        val goalString = timeMapper.formatInterval(
            interval = goal,
            forceSeconds = showSeconds,
            useProportionalMinutes = useProportionalMinutes,
        )
        val goalTimeString = "$currentString\n($goalString)"
        val goalTimePercent = (current * 100f / goal).roundToLong()
            .coerceAtMost(100)

        return StatisticsGoalViewData.Goal(
            goalTime = goalTimeString,
            goalPercent = goalTimePercent.let { "$it%" },
            goalComplete = goalComplete,
            percent = goalTimePercent,
        )
    }
}