package com.example.util.simpletimetracker.data_local.activityReminder

import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.daysOfWeek.mapper.DaysOfWeekDataLocalMapper
import javax.inject.Inject

class ActivityReminderDataLocalMapper @Inject constructor(
    private val daysOfWeekMapper: DaysOfWeekDataLocalMapper,
) {

    fun map(data: ActivityReminderOverrideWithRulesDBO): ActivityReminderOverride? {
        val mode = when (data.override.mode) {
            ActivityReminderOverrideDao.MODE_DISABLED -> {
                ActivityReminderOverride.Mode.Disabled
            }
            ActivityReminderOverrideDao.MODE_CUSTOM -> {
                // Only one rule allowed for one activity.
                // An invalid custom override must not accidentally inherit the global rule.
                data.rules.firstOrNull()?.let(::mapRule)
                    ?.let(ActivityReminderOverride.Mode::Custom)
                    ?: ActivityReminderOverride.Mode.Disabled
            }
            else -> return null
        }

        return ActivityReminderOverride(
            activityId = data.override.activityId,
            mode = mode,
        )
    }

    fun map(data: ActivityReminderOverride): ActivityReminderOverrideWithRulesDBO {
        val mode: Int
        val rules: List<ActivityReminderRuleDBO>

        when (val domainMode = data.mode) {
            ActivityReminderOverride.Mode.Disabled -> {
                mode = ActivityReminderOverrideDao.MODE_DISABLED
                rules = emptyList()
            }
            is ActivityReminderOverride.Mode.Custom -> {
                mode = ActivityReminderOverrideDao.MODE_CUSTOM
                rules = listOf(mapRule(domainMode.rule, data.activityId))
            }
        }
        return ActivityReminderOverrideWithRulesDBO(
            override = ActivityReminderOverrideDBO(
                activityId = data.activityId,
                mode = mode,
            ),
            rules = rules,
        )
    }

    private fun mapRule(
        data: ActivityReminderRuleDBO,
    ): ActivityReminderOverride.Rule {
        return ActivityReminderOverride.Rule(
            id = data.id,
            durationSeconds = data.durationSeconds,
            recurrent = data.recurrent,
            applicableDaysOfWeek = daysOfWeekMapper.mapDaysOfWeek(data.weekdays),
            doNotDisturbStartMillis = data.doNotDisturbStartMillis,
            doNotDisturbEndMillis = data.doNotDisturbEndMillis,
        )
    }

    private fun mapRule(
        data: ActivityReminderOverride.Rule,
        activityId: Long,
    ): ActivityReminderRuleDBO {
        return ActivityReminderRuleDBO(
            id = data.id,
            activityId = activityId,
            durationSeconds = data.durationSeconds,
            recurrent = data.recurrent,
            weekdays = daysOfWeekMapper.mapDaysOfWeek(data.applicableDaysOfWeek),
            doNotDisturbStartMillis = data.doNotDisturbStartMillis,
            doNotDisturbEndMillis = data.doNotDisturbEndMillis,
        )
    }
}
