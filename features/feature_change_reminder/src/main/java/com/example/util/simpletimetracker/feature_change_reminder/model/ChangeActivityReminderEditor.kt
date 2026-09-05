package com.example.util.simpletimetracker.feature_change_reminder.model

import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.orZero

class ChangeActivityReminderEditor private constructor(
    var mode: Mode,
    var ruleId: Long,
    var durationSeconds: Long,
    var recurrent: Boolean,
    var daysOfWeek: Set<DayOfWeek>,
    var doNotDisturbStartMillis: Long,
    var doNotDisturbEndMillis: Long,
) {

    fun selectMode(value: Mode) {
        mode = value
    }

    fun toggleDay(day: DayOfWeek) {
        daysOfWeek = daysOfWeek.addOrRemove(day)
    }

    fun toOverride(activityId: Long): ActivityReminderOverride {
        val overrideMode = when (mode) {
            Mode.DISABLED -> ActivityReminderOverride.Mode.Disabled
            Mode.CUSTOM -> ActivityReminderOverride.Mode.Custom(
                ActivityReminderOverride.Rule(
                    id = ruleId,
                    durationSeconds = durationSeconds,
                    recurrent = recurrent,
                    applicableDaysOfWeek = daysOfWeek.toSet(),
                    doNotDisturbStartMillis = doNotDisturbStartMillis,
                    doNotDisturbEndMillis = doNotDisturbEndMillis,
                ),
            )
        }
        return ActivityReminderOverride(
            activityId = activityId,
            mode = overrideMode,
        )
    }

    enum class Mode {
        DISABLED,
        CUSTOM,
    }

    companion object {
        fun new(
            defaultRule: ActivityReminderOverride.Rule,
        ): ChangeActivityReminderEditor {
            return ChangeActivityReminderEditor(
                mode = Mode.CUSTOM,
                ruleId = 0L,
                durationSeconds = defaultRule.durationSeconds,
                recurrent = defaultRule.recurrent,
                daysOfWeek = defaultRule.applicableDaysOfWeek.toMutableSet(),
                doNotDisturbStartMillis = defaultRule.doNotDisturbStartMillis,
                doNotDisturbEndMillis = defaultRule.doNotDisturbEndMillis,
            )
        }

        fun create(
            override: ActivityReminderOverride,
            defaultRule: ActivityReminderOverride.Rule,
        ): ChangeActivityReminderEditor {
            val customRule = (override.mode as? ActivityReminderOverride.Mode.Custom)?.rule
            val draftRule = customRule ?: defaultRule
            val mode = when (override.mode) {
                is ActivityReminderOverride.Mode.Disabled -> Mode.DISABLED
                is ActivityReminderOverride.Mode.Custom -> Mode.CUSTOM
            }
            return ChangeActivityReminderEditor(
                mode = mode,
                ruleId = customRule?.id.orZero(),
                durationSeconds = draftRule.durationSeconds,
                recurrent = draftRule.recurrent,
                daysOfWeek = draftRule.applicableDaysOfWeek.toMutableSet(),
                doNotDisturbStartMillis = draftRule.doNotDisturbStartMillis,
                doNotDisturbEndMillis = draftRule.doNotDisturbEndMillis,
            )
        }
    }
}
