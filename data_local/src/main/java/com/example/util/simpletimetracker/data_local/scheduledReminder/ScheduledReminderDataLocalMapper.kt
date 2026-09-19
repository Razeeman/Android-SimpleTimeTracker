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
                oneTimeDate = dbo.date.orZero(),
                timeOfDayMillis = dbo.timeOfDayMillis,
            )
            SCHEDULE_MONTHLY -> ScheduledReminder.Schedule.Monthly(
                dayOfMonth = dbo.monthlyDayOfMonth.orZero(),
                timeOfDayMillis = dbo.timeOfDayMillis,
            )
            SCHEDULE_HOURLY -> ScheduledReminder.Schedule.Hourly(
                intervalSeconds = dbo.intervalSeconds.orZero(),
                startDate = dbo.date.orZero(),
                timeOfDayMillis = dbo.timeOfDayMillis,
                daysOfWeek = dbo.weekdays?.let(daysOfWeekDataLocalMapper::mapDaysOfWeek).orEmpty(),
                doNotDisturbStartMillis = dbo.doNotDisturbStartMillis.orZero(),
                doNotDisturbEndMillis = dbo.doNotDisturbEndMillis.orZero(),
            )
            else -> ScheduledReminder.Schedule.Weekly(
                daysOfWeek = emptySet(),
                timeOfDayMillis = dbo.timeOfDayMillis,
            )
        }

        val condition: ScheduledReminder.Condition = when (dbo.conditionType) {
            CONDITION_ALWAYS -> ScheduledReminder.Condition.Always
            CONDITION_RECORDS_NOT_TRACKED -> if (dbo.targetId != null && dbo.targetId != 0L) {
                val target = when (dbo.targetType) {
                    TARGET_ACTIVITY -> ScheduledReminder.Condition.Target.Activity(dbo.targetId)
                    TARGET_CATEGORY -> ScheduledReminder.Condition.Target.Category(dbo.targetId)
                    TARGET_TAG -> ScheduledReminder.Condition.Target.Tag(dbo.targetId)
                    else -> null
                }
                target?.let(ScheduledReminder.Condition::RecordsNotTrackedToday)
                    ?: ScheduledReminder.Condition.Always
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
        val hourlyIntervalSeconds: Long?
        val hourlyDoNotDisturbStartMillis: Long?
        val hourlyDoNotDisturbEndMillis: Long?

        when (val schedule = domain.schedule) {
            is ScheduledReminder.Schedule.Weekly -> {
                scheduleType = SCHEDULE_WEEKLY
                timeOfDayMillis = schedule.timeOfDayMillis
                weekdays = daysOfWeekDataLocalMapper.mapDaysOfWeek(schedule.daysOfWeek)
                oneTimeDate = null
                monthlyDayOfMonth = null
                hourlyIntervalSeconds = null
                hourlyDoNotDisturbStartMillis = null
                hourlyDoNotDisturbEndMillis = null
            }
            is ScheduledReminder.Schedule.OneTime -> {
                scheduleType = SCHEDULE_ONE_TIME
                timeOfDayMillis = schedule.timeOfDayMillis
                weekdays = null
                oneTimeDate = schedule.oneTimeDate
                monthlyDayOfMonth = null
                hourlyIntervalSeconds = null
                hourlyDoNotDisturbStartMillis = null
                hourlyDoNotDisturbEndMillis = null
            }
            is ScheduledReminder.Schedule.Monthly -> {
                scheduleType = SCHEDULE_MONTHLY
                timeOfDayMillis = schedule.timeOfDayMillis
                weekdays = null
                oneTimeDate = null
                monthlyDayOfMonth = schedule.dayOfMonth
                hourlyIntervalSeconds = null
                hourlyDoNotDisturbStartMillis = null
                hourlyDoNotDisturbEndMillis = null
            }
            is ScheduledReminder.Schedule.Hourly -> {
                scheduleType = SCHEDULE_HOURLY
                timeOfDayMillis = schedule.timeOfDayMillis
                weekdays = daysOfWeekDataLocalMapper.mapDaysOfWeek(schedule.daysOfWeek)
                oneTimeDate = schedule.startDate
                monthlyDayOfMonth = null
                hourlyIntervalSeconds = schedule.intervalSeconds
                hourlyDoNotDisturbStartMillis = schedule.doNotDisturbStartMillis
                hourlyDoNotDisturbEndMillis = schedule.doNotDisturbEndMillis
            }
        }

        val conditionType: Int
        val targetId: Long?
        val targetType: Int
        when (val condition = domain.condition) {
            is ScheduledReminder.Condition.Always -> {
                conditionType = CONDITION_ALWAYS
                targetId = null
                targetType = TARGET_ACTIVITY
            }
            is ScheduledReminder.Condition.RecordsNotTrackedToday -> {
                conditionType = CONDITION_RECORDS_NOT_TRACKED
                targetId = condition.target.id
                targetType = when (condition.target) {
                    is ScheduledReminder.Condition.Target.Activity -> TARGET_ACTIVITY
                    is ScheduledReminder.Condition.Target.Category -> TARGET_CATEGORY
                    is ScheduledReminder.Condition.Target.Tag -> TARGET_TAG
                }
            }
        }

        return ScheduledReminderDBO(
            id = domain.id,
            enabled = domain.enabled,
            text = domain.text,
            scheduleType = scheduleType,
            timeOfDayMillis = timeOfDayMillis,
            weekdays = weekdays,
            date = oneTimeDate,
            monthlyDayOfMonth = monthlyDayOfMonth,
            intervalSeconds = hourlyIntervalSeconds,
            doNotDisturbStartMillis = hourlyDoNotDisturbStartMillis,
            doNotDisturbEndMillis = hourlyDoNotDisturbEndMillis,
            conditionType = conditionType,
            targetId = targetId,
            targetType = targetType,
        )
    }

    companion object {
        internal const val SCHEDULE_WEEKLY = 0
        internal const val SCHEDULE_ONE_TIME = 1
        internal const val SCHEDULE_MONTHLY = 2
        internal const val SCHEDULE_HOURLY = 3

        internal const val CONDITION_ALWAYS = 0
        internal const val CONDITION_RECORDS_NOT_TRACKED = 1

        internal const val TARGET_ACTIVITY = 0
        internal const val TARGET_CATEGORY = 1
        internal const val TARGET_TAG = 2
    }
}
