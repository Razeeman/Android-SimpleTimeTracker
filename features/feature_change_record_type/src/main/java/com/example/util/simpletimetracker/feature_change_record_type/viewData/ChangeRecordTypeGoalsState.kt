package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.domain.model.RecordTypeGoal

data class ChangeRecordTypeGoalsState(
    val session: RecordTypeGoal.Type? = null,
    val daily: RecordTypeGoal.Type? = null,
    val weekly: RecordTypeGoal.Type? = null,
    val monthly: RecordTypeGoal.Type? = null,
)