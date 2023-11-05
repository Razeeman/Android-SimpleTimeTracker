package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.StatisticsGoalViewData
import javax.inject.Inject
import kotlin.math.roundToLong

class GoalViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
) {

    fun mapForTimer(
        goal: RecordTypeGoal?,
        currentDuration: Long,
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result?,
        goalsVisible: Boolean,
    ): GoalTimeViewData {
        val noGoal = GoalTimeViewData(
            text = "",
            complete = false,
        )
        if (goal == null || goal.value <= 0L || !goalsVisible) {
            return noGoal
        }

        val typeString = when (goal.range) {
            is RecordTypeGoal.Range.Session -> R.string.change_record_type_session_goal_time
            is RecordTypeGoal.Range.Daily -> R.string.change_record_type_daily_goal_time
            is RecordTypeGoal.Range.Weekly -> R.string.change_record_type_weekly_goal_time
            is RecordTypeGoal.Range.Monthly -> R.string.change_record_type_monthly_goal_time
        }.let(resourceRepo::getString).lowercase()
        val goalValue = when (goal.type) {
            is RecordTypeGoal.Type.Duration -> goal.value * 1000
            is RecordTypeGoal.Type.Count -> goal.value
        }
        val current = when (goal.type) {
            is RecordTypeGoal.Type.Duration -> when (goal.range) {
                is RecordTypeGoal.Range.Session -> currentDuration
                is RecordTypeGoal.Range.Daily -> dailyCurrent?.duration.orZero()
                is RecordTypeGoal.Range.Weekly,
                is RecordTypeGoal.Range.Monthly,
                -> return noGoal
            }
            is RecordTypeGoal.Type.Count -> dailyCurrent?.count.orZero()
        }

        val valueLeft = goalValue - current
        val complete = valueLeft <= 0L
        val durationLeftString = if (complete) {
            typeString
        } else {
            val formatted = when (goal.type) {
                is RecordTypeGoal.Type.Duration -> mapDuration(
                    goalValue = valueLeft,
                    showSeconds = true,
                    useProportionalMinutes = false,
                )
                is RecordTypeGoal.Type.Count -> mapCount(
                    goalValue = valueLeft,
                )
            }

            "$typeString $formatted"
        }

        return GoalTimeViewData(
            text = durationLeftString,
            complete = complete,
        )
    }

    fun mapStatisticsList(
        goals: List<RecordTypeGoal>,
        types: Map<Long, RecordType>,
        filterType: ChartFilterType,
        filteredIds: List<Long>,
        rangeLength: RangeLength,
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<StatisticsGoalViewData> {
        val currentGoals: List<RecordTypeGoal> = when (filterType) {
            ChartFilterType.ACTIVITY -> {
                goals.filter { it.idData is RecordTypeGoal.IdData.Type }
            }
            ChartFilterType.CATEGORY -> {
                goals.filter { it.idData is RecordTypeGoal.IdData.Category }
            }
            else -> return emptyList()
        }
        if (rangeLength !in listOf(RangeLength.Day, RangeLength.Week, RangeLength.Month)) {
            return emptyList()
        }

        return currentGoals
            .filter {
                val typeId = (it.idData as? RecordTypeGoal.IdData.Type)?.value.orZero()
                val type = types[typeId.orZero()]
                when {
                    typeId != 0L && type == null -> false
                    typeId != 0L && type?.hidden.orFalse() -> false
                    it.idData.value in filteredIds -> false
                    rangeLength is RangeLength.Day -> it.range is RecordTypeGoal.Range.Daily
                    rangeLength is RangeLength.Week -> it.range is RecordTypeGoal.Range.Weekly
                    rangeLength is RangeLength.Month -> it.range is RecordTypeGoal.Range.Monthly
                    else -> false
                }
            }
            .mapNotNull { goal ->
                val id = goal.idData.value
                mapItem(
                    goal = goal,
                    statistics = statistics.firstOrNull { it.id == id },
                    dataHolder = data[id] ?: return@mapNotNull null,
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
            icon = dataHolder.icon
                ?.let(iconMapper::mapIcon),
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
            return mapDuration(
                goalValue = goalValue,
                showSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
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
        val goalHint = resourceRepo.getString(R.string.change_record_type_goal_time_hint)
            .lowercase()
        val goalString = "$goalHint - $goalValueString"
        val goalPercent = if (goalValue == 0L) {
            0
        } else {
            (current * 100f / goalValue).roundToLong().coerceAtMost(100)
        }

        return StatisticsGoalViewData.Goal(
            goalCurrent = currentValueString,
            goal = goalString,
            goalPercent = goalPercent.let { "$it%" },
            goalComplete = goalComplete,
            percent = goalPercent,
        )
    }

    private fun mapDuration(
        goalValue: Long,
        showSeconds: Boolean,
        useProportionalMinutes: Boolean,
    ): String {
        return timeMapper.formatInterval(
            interval = goalValue,
            forceSeconds = showSeconds,
            useProportionalMinutes = useProportionalMinutes,
        )
    }

    private fun mapCount(
        goalValue: Long,
    ): String {
        return "$goalValue " + resourceRepo.getQuantityString(
            stringResId = R.plurals.statistics_detail_times_tracked,
            quantity = goalValue.toInt(),
        )
    }
}