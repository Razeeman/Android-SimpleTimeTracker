package com.example.util.simpletimetracker.navigation.params

data class DateTimeDialogParams(
    val tag: String? = null,
    val type: DateTimeDialogType = DateTimeDialogType.DATETIME,
    val timestamp: Long
)