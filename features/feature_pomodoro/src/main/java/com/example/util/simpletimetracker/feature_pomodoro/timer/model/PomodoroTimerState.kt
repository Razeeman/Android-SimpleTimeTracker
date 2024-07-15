package com.example.util.simpletimetracker.feature_pomodoro.timer.model

data class PomodoroTimerState(
    val maxProgress: Int,
    val progress: Int,
    val timerUpdateMs: Long,
    val durationState: DurationState,
    val currentCycleHint: String,
) {

    data class DurationState(
        val textHours: String,
        val textMinutes: String,
        val textSeconds: String,
        val hoursIsVisible: Boolean,
    )
}