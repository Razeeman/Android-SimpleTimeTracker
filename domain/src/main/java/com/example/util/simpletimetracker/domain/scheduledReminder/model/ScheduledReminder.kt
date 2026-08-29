package com.example.util.simpletimetracker.domain.scheduledReminder.model

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek

data class ScheduledReminder(
    val id: Long = 0,
    val enabled: Boolean,
    val text: String,
    val schedule: Schedule,
    val condition: Condition,
) {

    sealed interface Schedule {

        data class Weekly(
            val daysOfWeek: Set<DayOfWeek>,
            val timeOfDayMillis: Long,
        ) : Schedule

        // Store the one-time date as a local epoch day rather than an instant
        // so it retains its selected calendar date across time-zone changes.
        data class OneTime(
            val oneTimeDate: Long,
            val timeOfDayMillis: Long,
        ) : Schedule

        data class Monthly(
            val dayOfMonth: Int,
            val timeOfDayMillis: Long,
        ) : Schedule
    }

    sealed interface Condition {
        data object Always : Condition
        data class ActivityNotTrackedToday(val activityId: Long) : Condition
    }
}
