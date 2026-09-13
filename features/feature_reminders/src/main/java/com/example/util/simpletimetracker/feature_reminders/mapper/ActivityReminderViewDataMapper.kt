package com.example.util.simpletimetracker.feature_reminders.mapper

import com.example.util.simpletimetracker.core.mapper.ChangeReminderViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_reminders.R
import com.example.util.simpletimetracker.feature_reminders.viewData.ReminderViewData
import com.example.util.simpletimetracker.feature_views.extension.joinToSpannable
import javax.inject.Inject

class ActivityReminderViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val changeReminderViewDataMapper: ChangeReminderViewDataMapper,
) {

    fun map(
        activity: RecordType,
        override: ActivityReminderOverride,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        firstDayOfWeek: DayOfWeek,
    ): ReminderViewData {
        val mode = override.mode
        val modeText = when (mode) {
            is ActivityReminderOverride.Mode.Disabled -> R.string.activity_reminder_mode_disabled
            is ActivityReminderOverride.Mode.Custom -> R.string.activity_reminder_mode_custom
        }.let(resourceRepo::getString)
        val summary = when (mode) {
            is ActivityReminderOverride.Mode.Disabled -> {
                resourceRepo.getString(R.string.activity_reminder_disabled_summary)
            }
            is ActivityReminderOverride.Mode.Custom -> mapRule(
                rule = mode.rule,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
                isDarkTheme = isDarkTheme,
            )
        }

        return ReminderViewData(
            id = activity.id,
            type = ReminderViewData.Type.ActivityReminder,
            title = activity.name,
            subtitle = modeText,
            summary = summary,
            enabled = true,
            backgroundColor = if (!activity.hidden) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            icon = iconMapper.mapIcon(activity.icon),
            iconBackgroundColor = colorMapper.mapToColorInt(activity.color, isDarkTheme),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            button = null,
        )
    }

    // TODO reuse ReminderSummaryMapper
    private fun mapRule(
        rule: ActivityReminderOverride.Rule,
        useMilitaryTime: Boolean,
        firstDayOfWeek: DayOfWeek,
        isDarkTheme: Boolean,
    ): CharSequence {
        val recurrence = if (rule.recurrent) {
            resourceRepo.getString(R.string.settings_inactivity_reminder_recurrent)
        } else {
            resourceRepo.getString(R.string.reminders_schedule_one_time)
        }

        val days = timeMapper.formatDays(
            firstDayOfWeek = firstDayOfWeek,
            selectedDaysOfWeek = rule.applicableDaysOfWeek,
        ).takeIf(String::isNotEmpty)

        val dnd = changeReminderViewDataMapper.mapDndHint(
            doNotDisturbStartMillis = rule.doNotDisturbStartMillis,
            doNotDisturbEndMillis = rule.doNotDisturbEndMillis,
            useMilitaryTime = useMilitaryTime,
            iconColor = resourceRepo.getThemedAttr(R.attr.appLightTextColor, isDarkTheme)
        )

        return listOfNotNull(
            timeMapper.formatDuration(rule.durationSeconds),
            recurrence,
            days,
            dnd,
        ).joinToSpannable(separator = " · ")
    }
}
