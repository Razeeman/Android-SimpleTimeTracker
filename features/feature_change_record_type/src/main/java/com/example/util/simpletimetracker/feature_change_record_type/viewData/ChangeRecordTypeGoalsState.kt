package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal

data class ChangeRecordTypeGoalsState(
    val session: RecordTypeGoal.Type,
    val daily: RecordTypeGoal.Type,
    val weekly: RecordTypeGoal.Type,
    val monthly: RecordTypeGoal.Type,
    val daysOfWeek: List<DayOfWeek>,
)