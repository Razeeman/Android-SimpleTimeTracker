package com.example.util.simpletimetracker.domain.scheduledReminder.model

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.model.RecordTimerEvent

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

        data class Hourly(
            val intervalSeconds: Long,
            val startDate: Long,
            val daysOfWeek: Set<DayOfWeek>,
            val doNotDisturbStartMillis: Long,
            val doNotDisturbEndMillis: Long,
            val timeOfDayMillis: Long,
        ) : Schedule

        data class ActivityEvent(
            val event: RecordTimerEvent,
            val target: Condition.Target,
        ) : Schedule
    }

    sealed interface Condition {
        data object Always : Condition

        data class RecordsNotTrackedToday(
            val target: Target,
        ) : Condition

        sealed interface Target {
            val id: Long

            data class Activity(override val id: Long) : Target
            data class Category(override val id: Long) : Target
            data class Tag(override val id: Long) : Target
        }
    }
}
