package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import javax.inject.Inject

class GoalTimeMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
) {

    fun map(
        goalTime: Long,
        current: Long,
        type: GoalTimeType,
    ): GoalTimeViewData {
        if (goalTime <= 0L) return GoalTimeViewData(
            text = "",
            complete = false
        )

        val typeString = when (type) {
            is GoalTimeType.Session -> R.string.change_record_type_session_goal_time
            is GoalTimeType.Day -> R.string.change_record_type_daily_goal_time
            is GoalTimeType.Week -> R.string.change_record_type_weekly_goal_time
            is GoalTimeType.Month -> R.string.change_record_type_monthly_goal_time
        }.let(resourceRepo::getString).lowercase()

        val durationLeft = goalTime * 1000 - current
        val complete = durationLeft <= 0L
        val durationLeftString = if (complete) {
            typeString
        } else {
            val formatted = timeMapper.formatInterval(
                interval = durationLeft,
                forceSeconds = true,
                useProportionalMinutes = false
            )
            "$typeString $formatted"
        }

        return GoalTimeViewData(
            text = durationLeftString,
            complete = complete
        )
    }
}