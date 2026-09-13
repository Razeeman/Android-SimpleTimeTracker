package com.example.util.simpletimetracker.feature_reminders.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.ChangeReminderViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_reminders.viewData.ReminderViewData
import com.example.util.simpletimetracker.feature_reminders.viewData.RemindersButtonViewData
import com.example.util.simpletimetracker.feature_views.extension.joinToSpannable
import java.time.LocalDate
import java.util.TimeZone
import javax.inject.Inject
import com.example.util.simpletimetracker.core.R as coreR

class ReminderViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val localDateMapper: LocalDateMapper,
    private val changeReminderViewDataMapper: ChangeReminderViewDataMapper,
) {

    fun mapAddItem(
        id: RemindersButtonViewData,
        isDarkTheme: Boolean,
    ): ButtonViewData {
        return ButtonViewData(
            id = id,
            text = resourceRepo.getString(R.string.running_records_add_type),
            icon = ButtonViewData.Icon.Hidden,
            backgroundColor = resourceRepo.getThemedAttr(coreR.attr.appInactiveColor, isDarkTheme),
            isEnabled = true,
            marginHorizontalDp = 4,
        )
    }

    fun map(
        reminder: ScheduledReminder,
        activity: RecordType?,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        firstDayOfWeek: DayOfWeek,
    ): ReminderViewData {
        return ReminderViewData(
            id = reminder.id,
            type = ReminderViewData.Type.ScheduledReminder,
            title = reminder.text,
            subtitle = mapSchedule(
                schedule = reminder.schedule,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
                isDarkTheme = isDarkTheme,
            ),
            summary = mapCondition(
                condition = reminder.condition,
                activity = activity,
            ),
            enabled = reminder.enabled,
            backgroundColor = if (reminder.enabled) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            icon = activity?.icon
                ?.let(iconMapper::mapIcon),
            iconBackgroundColor = activity?.color
                ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                ?: colorMapper.toInactiveColor(isDarkTheme),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            button = ReminderViewData.Button(
                enabledButtonColor = if (reminder.enabled) {
                    colorMapper.toInactiveColor(isDarkTheme)
                } else {
                    colorMapper.toActiveColor(isDarkTheme)
                },
                enabledButtonText = if (reminder.enabled) {
                    R.string.complex_rules_disable
                } else {
                    R.string.complex_rules_enable
                }.let(resourceRepo::getString),
            ),
        )
    }

    private fun mapSchedule(
        schedule: ScheduledReminder.Schedule,
        useMilitaryTime: Boolean,
        firstDayOfWeek: DayOfWeek,
        isDarkTheme: Boolean,
    ): CharSequence {
        // Specified type prevents accidental nulls.
        val hints: List<CharSequence> = when (schedule) {
            is ScheduledReminder.Schedule.Weekly -> {
                val time = formatTime(
                    timeOfDayMillis = schedule.timeOfDayMillis,
                    useMilitaryTime = useMilitaryTime,
                )
                val days = timeMapper.formatDays(
                    firstDayOfWeek = firstDayOfWeek,
                    selectedDaysOfWeek = schedule.daysOfWeek,
                ).takeIf(String::isNotEmpty)
                    ?: resourceRepo.getString(R.string.reminders_schedule_daily)
                listOfNotNull(days, time)
            }
            is ScheduledReminder.Schedule.OneTime -> {
                val timestamp = resolve(
                    dateEpochDay = schedule.oneTimeDate,
                    timeOfDayMillis = schedule.timeOfDayMillis,
                )
                val dateTime = timeMapper.formatDateTimeYear(
                    time = timestamp,
                    useMilitaryTime = useMilitaryTime,
                )
                val hint = resourceRepo.getString(R.string.reminders_schedule_one_time)
                listOfNotNull(hint, dateTime)
            }
            is ScheduledReminder.Schedule.Monthly -> {
                val time = formatTime(
                    timeOfDayMillis = schedule.timeOfDayMillis,
                    useMilitaryTime = useMilitaryTime,
                )
                val hint = resourceRepo.getString(R.string.reminders_schedule_monthly)
                listOfNotNull(hint, schedule.dayOfMonth.toString(), time)
            }
            is ScheduledReminder.Schedule.Hourly -> {
                val hint = resourceRepo.getString(R.string.reminders_schedule_hourly)
                val interval = timeMapper.formatDuration(schedule.intervalSeconds)
                val startTimestamp = resolve(
                    dateEpochDay = schedule.startDate,
                    timeOfDayMillis = schedule.timeOfDayMillis,
                )
                val start = resourceRepo.getString(
                    R.string.separator_template,
                    resourceRepo.getString(R.string.change_record_date_time_start),
                    timeMapper.formatDateTime(
                        time = startTimestamp,
                        useMilitaryTime = useMilitaryTime,
                        showSeconds = false,
                    ),
                )
                val days = timeMapper.formatDays(
                    firstDayOfWeek = firstDayOfWeek,
                    selectedDaysOfWeek = schedule.daysOfWeek,
                ).takeIf(String::isNotEmpty)
                    ?: resourceRepo.getString(R.string.reminders_schedule_daily)
                val dnd = changeReminderViewDataMapper.mapDndHint(
                    doNotDisturbStartMillis = schedule.doNotDisturbStartMillis,
                    doNotDisturbEndMillis = schedule.doNotDisturbEndMillis,
                    useMilitaryTime = useMilitaryTime,
                    iconColor = resourceRepo.getThemedAttr(R.attr.appLightTextColor, isDarkTheme),
                )
                listOfNotNull(hint, interval, start, days, dnd)
            }
        }
        return hints.joinToSpannable(separator = " · ")
    }

    private fun mapCondition(
        condition: ScheduledReminder.Condition,
        activity: RecordType?,
    ): CharSequence {
        return when (condition) {
            is ScheduledReminder.Condition.Always -> ""
            is ScheduledReminder.Condition.ActivityNotTrackedToday -> {
                val activityName = activity?.name
                    ?: resourceRepo.getString(R.string.no_data)
                val hint = resourceRepo.getString(R.string.reminders_condition_activity_not_tracked)
                "$hint ($activityName)"
            }
        }
    }

    private fun formatTime(
        timeOfDayMillis: Long,
        useMilitaryTime: Boolean,
    ): String {
        val timeZone = TimeZone.getDefault()
        return changeReminderViewDataMapper.formatTimeOfDay(
            millis = timeOfDayMillis,
            useMilitaryTime = useMilitaryTime,
            date = LocalDate.now(timeZone.toZoneId()),
            timeZone = timeZone,
        )
    }

    private fun resolve(dateEpochDay: Long, timeOfDayMillis: Long): Long {
        return localDateMapper.resolveDateTime(
            dateEpochDay = dateEpochDay,
            timeOfDayMillis = timeOfDayMillis,
            timeZone = TimeZone.getDefault(),
        ).orZero()
    }
}
