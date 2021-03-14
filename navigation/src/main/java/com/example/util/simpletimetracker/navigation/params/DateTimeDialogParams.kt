package com.example.util.simpletimetracker.navigation.params

data class DateTimeDialogParams(
    val tag: String? = null,
    val useMilitaryTime: Boolean = false,
    val type: DateTimeDialogType,
    val timestamp: Long
)