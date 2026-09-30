package com.example.util.simpletimetracker.feature_goals.model

import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal

data class GoalsSetupData(
    val isDarkTheme: Boolean,
    val durationFormat: DurationFormat,
    val showSeconds: Boolean,
    val firstDayOfWeek: DayOfWeek,
    val startOfDayShift: Long,
    val hideFinishedGoals: Boolean,
    val types: Map<Long, RecordType>,
    val goals: List<RecordTypeGoal>,
)