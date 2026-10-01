package com.example.util.simpletimetracker.feature_change_goals.viewData

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal

data class ChangeRecordTypeGoalsState(
    val data: List<GoalState>,
    val expandedGoalKey: Long?,
) {

    data class GoalState(
        val key: Long,
        val id: Long,
        val range: RecordTypeGoal.Range,
        val type: RecordTypeGoal.Type,
        val subtype: RecordTypeGoal.Subtype,
        val daysOfWeek: Set<DayOfWeek>,
        val requestScroll: Boolean,
    )
}