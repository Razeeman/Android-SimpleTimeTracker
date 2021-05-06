package com.example.util.simpletimetracker.navigation.params

import com.example.util.simpletimetracker.domain.model.DayOfWeek

data class DateTimeDialogParams(
    val tag: String? = null,
    val useMilitaryTime: Boolean,
    val type: DateTimeDialogType,
    val timestamp: Long,
    val firstDayOfWeek: DayOfWeek
)