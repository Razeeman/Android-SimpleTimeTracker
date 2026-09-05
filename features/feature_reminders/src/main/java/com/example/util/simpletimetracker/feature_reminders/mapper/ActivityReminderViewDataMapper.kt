package com.example.util.simpletimetracker.feature_reminders.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import com.example.util.simpletimetracker.feature_reminders.R
import com.example.util.simpletimetracker.feature_reminders.viewData.ActivityReminderViewData
import java.time.LocalDate
import java.util.TimeZone
import javax.inject.Inject

class ActivityReminderViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val localDateMapper: LocalDateMapper,
) {

    fun map(
        activity: RecordType,
        override: ActivityReminderOverride,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        firstDayOfWeek: DayOfWeek,
    ): ActivityReminderViewData {
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
            )
        }

        return ActivityReminderViewData(
            activityId = activity.id,
            name = activity.name,
            mode = modeText,
            summary = summary,
            icon = iconMapper.mapIcon(activity.icon),
            iconBackgroundColor = colorMapper.mapToColorInt(activity.color, isDarkTheme),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            backgroundColor = if (!activity.hidden) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
        )
    }

    // TODO reuse ReminderSummaryMapper
    private fun mapRule(
        rule: ActivityReminderOverride.Rule,
        useMilitaryTime: Boolean,
        firstDayOfWeek: DayOfWeek,
    ): String {
        val recurrence = if (rule.recurrent) {
            resourceRepo.getString(R.string.settings_inactivity_reminder_recurrent)
        } else {
            resourceRepo.getString(R.string.reminders_schedule_one_time)
        }

        val days = timeMapper.formatDays(
            firstDayOfWeek = firstDayOfWeek,
            selectedDaysOfWeek = rule.applicableDaysOfWeek,
        ).takeIf(String::isNotEmpty)

        val dndStart = formatTimeOfDay(rule.doNotDisturbStartMillis, useMilitaryTime)
        val dndEnd = formatTimeOfDay(rule.doNotDisturbEndMillis, useMilitaryTime)
        val dnd = "$dndStart-$dndEnd"

        return listOfNotNull(
            timeMapper.formatDuration(rule.durationSeconds),
            recurrence,
            days,
            dnd,
        ).joinToString(separator = " · ")
    }

    private fun formatTimeOfDay(
        timeOfDayMillis: Long,
        useMilitaryTime: Boolean,
    ): String {
        val timeZone = TimeZone.getDefault()
        val date = LocalDate.now(timeZone.toZoneId())
        val timestamp = localDateMapper.resolveDateTime(
            date = date,
            timeOfDayMillis = timeOfDayMillis,
            timeZone = timeZone,
        ) ?: return resourceRepo.getString(R.string.no_data)

        return timeMapper.formatTime(
            time = timestamp,
            useMilitaryTime = useMilitaryTime,
            showSeconds = false,
        )
    }
}
