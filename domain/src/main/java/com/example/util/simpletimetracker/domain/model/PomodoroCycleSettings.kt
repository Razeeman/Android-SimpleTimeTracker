package com.example.util.simpletimetracker.domain.model

data class PomodoroCycleSettings(
    val focusTimeMs: Long,
    val breakTimeMs: Long,
    val longBreakTimeMs: Long,
    val periodsUntilLongBreak: Long,
)