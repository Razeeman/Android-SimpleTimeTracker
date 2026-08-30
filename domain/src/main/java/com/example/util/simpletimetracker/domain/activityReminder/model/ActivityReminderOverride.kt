package com.example.util.simpletimetracker.domain.activityReminder.model

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek

data class ActivityReminderOverride(
    val activityId: Long,
    val mode: Mode,
) {
    sealed interface Mode {
        data object Disabled : Mode

        // Only one rule per activity is allowed in order to avoid complex scheduling logic.
        data class Custom(val rule: Rule) : Mode
    }

    data class Rule(
        val id: Long,
        val durationSeconds: Long,
        val recurrent: Boolean,
        val applicableDaysOfWeek: Set<DayOfWeek>,
        val doNotDisturbStartMillis: Long,
        val doNotDisturbEndMillis: Long,
    )
}
