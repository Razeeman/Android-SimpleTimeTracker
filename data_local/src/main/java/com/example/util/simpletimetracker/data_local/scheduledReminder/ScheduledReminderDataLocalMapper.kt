package com.example.util.simpletimetracker.data_local.scheduledReminder

import com.example.util.simpletimetracker.domain.daysOfWeek.mapper.DaysOfWeekDataLocalMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import javax.inject.Inject

class ScheduledReminderDataLocalMapper @Inject constructor(
    private val daysOfWeekDataLocalMapper: DaysOfWeekDataLocalMapper,
) {

    fun map(dbo: ScheduledReminderDBO): ScheduledReminder {
        val schedule: ScheduledReminder.Schedule = when (dbo.scheduleType) {
            SCHEDULE_WEEKLY -> ScheduledReminder.Schedule.Weekly(
                daysOfWeek = dbo.weekdays?.let(daysOfWeekDataLocalMapper::mapDaysOfWeek).orEmpty(),
                timeOfDayMillis = dbo.timeOfDayMillis,
            )
            SCHEDULE_ONE_TIME -> ScheduledReminder.Schedule.OneTime(
                oneTimeDate = dbo.oneTimeDate.orZero(),
                timeOfDayMillis = dbo.timeOfDayMillis,
            )
            SCHEDULE_MONTHLY -> ScheduledReminder.Schedule.Monthly(
                dayOfMonth = dbo.monthlyDayOfMonth.orZero(),
                timeOfDayMillis = dbo.timeOfDayMillis,
            )
            else -> ScheduledReminder.Schedule.Weekly(
                daysOfWeek = emptySet(),
                timeOfDayMillis = dbo.timeOfDayMillis,
            )
        }

        val condition: ScheduledReminder.Condition = when (dbo.conditionType) {
            CONDITION_ALWAYS -> ScheduledReminder.Condition.Always
            CONDITION_ACTIVITY_NOT_TRACKED -> if (dbo.activityId != null && dbo.activityId != 0L) {
                ScheduledReminder.Condition.ActivityNotTrackedToday(activityId = dbo.activityId)
            } else {
                ScheduledReminder.Condition.Always
            }
            else -> ScheduledReminder.Condition.Always
        }

        return ScheduledReminder(
            id = dbo.id,
            enabled = dbo.enabled,
            text = dbo.text,
            schedule = schedule,
            condition = condition,
        )
    }

    fun map(domain: ScheduledReminder): ScheduledReminderDBO {
        val scheduleType: Int
        val timeOfDayMillis: Long
        val weekdays: String?
        val oneTimeDate: Long?
        val monthlyDayOfMonth: Int?

        when (val schedule = domain.schedule) {
            is ScheduledReminder.Schedule.Weekly -> {
                scheduleType = SCHEDULE_WEEKLY
                timeOfDayMillis = schedule.timeOfDayMillis
                weekdays = daysOfWeekDataLocalMapper.mapDaysOfWeek(schedule.daysOfWeek)
                oneTimeDate = null
                monthlyDayOfMonth = null
            }
            is ScheduledReminder.Schedule.OneTime -> {
                scheduleType = SCHEDULE_ONE_TIME
                timeOfDayMillis = schedule.timeOfDayMillis
                weekdays = null
                oneTimeDate = schedule.oneTimeDate
                monthlyDayOfMonth = null
            }
            is ScheduledReminder.Schedule.Monthly -> {
                scheduleType = SCHEDULE_MONTHLY
                timeOfDayMillis = schedule.timeOfDayMillis
                weekdays = null
                oneTimeDate = null
                monthlyDayOfMonth = schedule.dayOfMonth
            }
        }

        val conditionType: Int
        val activityId: Long?
        when (val condition = domain.condition) {
            is ScheduledReminder.Condition.Always -> {
                conditionType = CONDITION_ALWAYS
                activityId = null
            }
            is ScheduledReminder.Condition.ActivityNotTrackedToday -> {
                conditionType = CONDITION_ACTIVITY_NOT_TRACKED
                activityId = condition.activityId
            }
        }

        return ScheduledReminderDBO(
            id = domain.id,
            enabled = domain.enabled,
            text = domain.text,
            scheduleType = scheduleType,
            timeOfDayMillis = timeOfDayMillis,
            weekdays = weekdays,
            oneTimeDate = oneTimeDate,
            monthlyDayOfMonth = monthlyDayOfMonth,
            conditionType = conditionType,
            activityId = activityId,
        )
    }

    companion object {
        internal const val SCHEDULE_WEEKLY = 0
        internal const val SCHEDULE_ONE_TIME = 1
        internal const val SCHEDULE_MONTHLY = 2

        internal const val CONDITION_ALWAYS = 0
        internal const val CONDITION_ACTIVITY_NOT_TRACKED = 1
    }
}
