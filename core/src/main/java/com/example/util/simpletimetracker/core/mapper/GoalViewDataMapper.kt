package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.StatisticsGoalViewData
import javax.inject.Inject
import kotlin.math.roundToLong

class GoalViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
) {

    fun mapList(
        goals: List<RecordTypeGoal>,
        types: Map<Long, RecordType>,
        filterType: ChartFilterType,
        rangeLength: RangeLength,
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<StatisticsGoalViewData> {
        if (filterType != ChartFilterType.ACTIVITY) {
            return emptyList()
        }
        if (rangeLength !in listOf(RangeLength.Day, RangeLength.Week, RangeLength.Month)) {
            return emptyList()
        }

        return goals
            .filter {
                val type = types[it.typeId]
                when {
                    type == null -> false
                    type.hidden -> false
                    rangeLength is RangeLength.Day -> it.range is RecordTypeGoal.Range.Daily
                    rangeLength is RangeLength.Week -> it.range is RecordTypeGoal.Range.Weekly
                    rangeLength is RangeLength.Month -> it.range is RecordTypeGoal.Range.Monthly
                    else -> false
                }
            }
            .mapNotNull { goal ->
                mapItem(
                    goal = goal,
                    statistics = statistics.firstOrNull { it.id == goal.typeId },
                    dataHolder = data[goal.typeId] ?: return@mapNotNull null,
                    isDarkTheme = isDarkTheme,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                )
            }
            .sortedBy { it.goal.percent }
    }

    private fun mapItem(
        goal: RecordTypeGoal,
        statistics: Statistics?,
        dataHolder: StatisticsDataHolder,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): StatisticsGoalViewData {
        return StatisticsGoalViewData(
            id = goal.id,
            name = dataHolder.name,
            icon = dataHolder.icon.orEmpty()
                .let(iconMapper::mapIcon),
            color = dataHolder.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
            goal = mapGoal(
                goal = goal,
                statistics = statistics,
                useProportionalMinutes = useProportionalMinutes,
                showSeconds = showSeconds,
            ),
        )
    }

    private fun mapGoal(
        goal: RecordTypeGoal,
        statistics: Statistics?,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): StatisticsGoalViewData.Goal {
        fun mapDuration(goalValue: Long): String {
            return timeMapper.formatInterval(
                interval = goalValue,
                forceSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
            )
        }
        fun mapCount(goalValue: Long): String {
            return "$goalValue " + resourceRepo.getQuantityString(
                stringResId = R.plurals.statistics_detail_times_tracked,
                quantity = goalValue.toInt(),
            )
        }

        val goalValue = when (goal.type) {
            is RecordTypeGoal.Type.Duration -> goal.value * 1000
            is RecordTypeGoal.Type.Count -> goal.value
        }
        val current = when (goal.type) {
            is RecordTypeGoal.Type.Duration -> statistics?.data?.duration.orZero()
            is RecordTypeGoal.Type.Count -> statistics?.data?.count.orZero()
        }

        val goalComplete = goalValue - current <= 0L
        val (currentValueString, goalValueString) = when (goal.type) {
            is RecordTypeGoal.Type.Duration -> {
                mapDuration(current) to mapDuration(goalValue)
            }
            is RecordTypeGoal.Type.Count -> {
                mapCount(current) to mapCount(goalValue)
            }
        }
        val goalString = "$currentValueString\n($goalValueString)"
        val goalPercent = if (goalValue == 0L) {
            0
        } else {
            (current * 100f / goalValue).roundToLong().coerceAtMost(100)
        }

        return StatisticsGoalViewData.Goal(
            goalTime = goalString,
            goalPercent = goalPercent.let { "$it%" },
            goalComplete = goalComplete,
            percent = goalPercent,
        )
    }
}