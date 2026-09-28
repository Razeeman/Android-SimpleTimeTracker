package com.example.util.simpletimetracker.feature_base_adapter.runningRecord

import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal

data class GoalTimeViewData(
    val text: String,
    val state: RecordTypeGoal.Subtype?,
)