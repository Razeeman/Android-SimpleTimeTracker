package com.example.util.simpletimetracker.feature_reminders.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderOccurrenceCalculator
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_reminders.viewData.ReminderViewData
import com.example.util.simpletimetracker.feature_reminders.viewData.RemindersButtonViewData
import java.time.LocalDate
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.example.util.simpletimetracker.core.R as coreR

class ReminderViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val scheduledReminderOccurrenceCalculator: ScheduledReminderOccurrenceCalculator,
) {

    fun mapAddItem(isDarkTheme: Boolean): ButtonViewData {
        return ButtonViewData(
            id = RemindersButtonViewData,
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
        timeZone: TimeZone,
    ): ReminderViewData {
        return ReminderViewData(
            id = reminder.id,
            text = reminder.text,
            scheduleSummary = mapSchedule(
                schedule = reminder.schedule,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
                timeZone = timeZone,
            ),
            conditionSummary = mapCondition(
                condition = reminder.condition,
                activity = activity,
            ),
            enabled = reminder.enabled,
            backgroundColor = if (reminder.enabled) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
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
            activityIcon = activity?.icon
                ?.let(iconMapper::mapIcon),
            activityColor = activity?.color
                ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                ?: colorMapper.toInactiveColor(isDarkTheme),
            activityIconColor = colorMapper.toIconColor(isDarkTheme),
        )
    }

    private fun mapSchedule(
        schedule: ScheduledReminder.Schedule,
        useMilitaryTime: Boolean,
        firstDayOfWeek: DayOfWeek,
        timeZone: TimeZone,
    ): String {
        return when (schedule) {
            is ScheduledReminder.Schedule.Weekly -> {
                val time = formatTime(
                    timeOfDayMillis = schedule.timeOfDayMillis,
                    useMilitaryTime = useMilitaryTime,
                    timeZone = timeZone,
                )
                if (schedule.daysOfWeek.size == DayOfWeek.entries.size) {
                    val hint = resourceRepo.getString(R.string.reminders_schedule_daily)
                    "$hint $time"
                } else {
                    val days = timeMapper.getWeekOrder(firstDayOfWeek)
                        .filter(schedule.daysOfWeek::contains)
                        .joinToString(separator = ", ", transform = timeMapper::toShortDayOfWeekName)
                    "$days $time"
                }
            }
            is ScheduledReminder.Schedule.OneTime -> {
                val timestamp = resolve(
                    dateEpochDay = schedule.oneTimeDate,
                    timeOfDayMillis = schedule.timeOfDayMillis,
                    timeZone = timeZone,
                )
                val dateTime = timeMapper.formatDateTimeYear(
                    time = timestamp,
                    useMilitaryTime = useMilitaryTime,
                )
                val hint = resourceRepo.getString(R.string.reminders_schedule_one_time)
                "$hint $dateTime"
            }
            is ScheduledReminder.Schedule.Monthly -> {
                val time = formatTime(
                    timeOfDayMillis = schedule.timeOfDayMillis,
                    useMilitaryTime = useMilitaryTime,
                    timeZone = timeZone,
                )
                val hint = resourceRepo.getString(R.string.reminders_schedule_monthly)
                "$hint ${schedule.dayOfMonth} $time"
            }
        }
    }

    private fun mapCondition(
        condition: ScheduledReminder.Condition,
        activity: RecordType?,
    ): String {
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
        timeZone: TimeZone,
    ): String {
        val timestamp = resolve(
            dateEpochDay = LocalDate.now(timeZone.toZoneId()).toEpochDay(),
            timeOfDayMillis = timeOfDayMillis,
            timeZone = timeZone,
        )
        return timeMapper.formatTime(
            time = timestamp,
            useMilitaryTime = useMilitaryTime,
            showSeconds = false,
        )
    }

    private fun resolve(dateEpochDay: Long, timeOfDayMillis: Long, timeZone: TimeZone): Long {
        val timeOfDayMillis = timeOfDayMillis.coerceIn(0, TimeUnit.DAYS.toMillis(1) - 1)

        return scheduledReminderOccurrenceCalculator.resolveLocalDateTime(
            dateEpochDay = dateEpochDay,
            timeOfDayMillis = timeOfDayMillis,
            timeZone = timeZone,
        )
    }
}
