package com.example.util.simpletimetracker.feature_change_reminder.model

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.toLocalDateTime
import com.example.util.simpletimetracker.domain.record.model.RecordTimerEvent
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import com.example.util.simpletimetracker.feature_change_reminder.utils.isActivityEvent
import java.time.LocalDate
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class ChangeReminderEditor private constructor(
    val id: Long,
    val enabled: Boolean,
    var message: String,
    private var messageTouched: Boolean,
    var scheduleType: ScheduleType,
    var daysOfWeek: Set<DayOfWeek>,
    var date: Long,
    var dayOfMonth: Int,
    var timeOfDayMillis: Long,
    var intervalSeconds: Long,
    var doNotDisturbStartMillis: Long,
    var doNotDisturbEndMillis: Long,
    var conditionType: ConditionType,
    var conditionTarget: ScheduledReminder.Condition.Target?,
) {

    fun onMessageChanged(value: String) {
        message = value
        messageTouched = true
    }

    fun selectSchedule(type: ScheduleType) {
        val keepEventTarget = scheduleType.isActivityEvent() && type.isActivityEvent()
        scheduleType = type
        conditionType = ConditionType.ALWAYS
        if (!keepEventTarget) conditionTarget = null
    }

    fun selectCondition(type: ConditionType) {
        if (scheduleType != ScheduleType.WEEKLY) {
            conditionType = ConditionType.ALWAYS
            conditionTarget = null
            return
        }
        conditionType = type
        if (type == ConditionType.ALWAYS) conditionTarget = null
    }

    fun toggleDay(day: DayOfWeek) {
        daysOfWeek = daysOfWeek.addOrRemove(day)
    }

    fun clearTarget() {
        conditionTarget = null
    }

    fun selectTarget(
        target: ScheduledReminder.Condition.Target,
        prefill: () -> String,
    ) {
        conditionType = if (scheduleType.isActivityEvent()) {
            ConditionType.ALWAYS
        } else {
            ConditionType.NOT_TRACKED
        }
        conditionTarget = target
        if (!scheduleType.isActivityEvent() && (!messageTouched || message.isBlank())) {
            message = prefill()
            messageTouched = false
        }
    }

    fun validate(
        nowTimestamp: Long,
        localDateMapper: LocalDateMapper,
    ): ValidationResult {
        if (message.trim().isEmpty()) {
            return ValidationResult.Error(ValidationError.MESSAGE_REQUIRED)
        }
        if (timeOfDayMillis !in 0 until TimeUnit.DAYS.toMillis(1)) {
            timeOfDayMillis = 0
        }

        val schedule = when (scheduleType) {
            ScheduleType.WEEKLY -> {
                if (daysOfWeek.isEmpty()) daysOfWeek = DayOfWeek.entries.toSet()
                ScheduledReminder.Schedule.Weekly(daysOfWeek, timeOfDayMillis)
            }
            ScheduleType.ONE_TIME -> {
                val expectedTimestamp = localDateMapper.resolveDateTime(
                    dateEpochDay = date,
                    timeOfDayMillis = timeOfDayMillis,
                    timeZone = TimeZone.getDefault(),
                ).orZero()
                if (expectedTimestamp <= nowTimestamp) {
                    return ValidationResult.Error(ValidationError.FUTURE_REQUIRED)
                }
                ScheduledReminder.Schedule.OneTime(date, timeOfDayMillis)
            }
            ScheduleType.MONTHLY -> {
                if (dayOfMonth !in 1..DAYS_IN_MONTH) dayOfMonth = 1
                ScheduledReminder.Schedule.Monthly(dayOfMonth, timeOfDayMillis)
            }
            ScheduleType.HOURLY -> {
                if (intervalSeconds <= 0L) {
                    return ValidationResult.Error(ValidationError.INTERVAL_REQUIRED)
                }
                if (daysOfWeek.isEmpty()) daysOfWeek = DayOfWeek.entries.toSet()
                ScheduledReminder.Schedule.Hourly(
                    intervalSeconds = intervalSeconds,
                    startDate = date,
                    timeOfDayMillis = timeOfDayMillis,
                    daysOfWeek = daysOfWeek,
                    doNotDisturbStartMillis = doNotDisturbStartMillis,
                    doNotDisturbEndMillis = doNotDisturbEndMillis,
                )
            }
            ScheduleType.ACTIVITY_STARTED,
            ScheduleType.ACTIVITY_STOPPED,
            -> {
                val target = conditionTarget?.takeIf { it.id > 0L }
                    ?: return ValidationResult.Error(ValidationError.TARGET_REQUIRED)
                ScheduledReminder.Schedule.ActivityEvent(
                    event = if (scheduleType == ScheduleType.ACTIVITY_STARTED) {
                        RecordTimerEvent.STARTED
                    } else {
                        RecordTimerEvent.STOPPED
                    },
                    target = target,
                )
            }
        }

        val condition = when {
            scheduleType != ScheduleType.WEEKLY -> ScheduledReminder.Condition.Always
            conditionType == ConditionType.NOT_TRACKED -> {
                conditionTarget
                    ?.let { ScheduledReminder.Condition.RecordsNotTrackedToday(it) }
                    ?: ScheduledReminder.Condition.Always
            }
            else -> ScheduledReminder.Condition.Always
        }

        return ValidationResult.Valid(
            reminder = ScheduledReminder(
                id = id,
                enabled = enabled,
                text = message,
                schedule = schedule,
                condition = condition,
            ),
        )
    }

    enum class ScheduleType {
        WEEKLY,
        ONE_TIME,
        MONTHLY,
        HOURLY,
        ACTIVITY_STARTED,
        ACTIVITY_STOPPED,
    }

    enum class ConditionType {
        ALWAYS,
        NOT_TRACKED,
    }

    enum class ValidationError {
        MESSAGE_REQUIRED,
        FUTURE_REQUIRED,
        INTERVAL_REQUIRED,
        TARGET_REQUIRED,
    }

    sealed interface ValidationResult {
        data class Valid(val reminder: ScheduledReminder) : ValidationResult
        data class Error(val error: ValidationError) : ValidationResult
    }

    companion object {
        const val DAYS_IN_MONTH = 31

        private fun getTomorrow(nowTimestamp: Long): LocalDate {
            return nowTimestamp
                .toLocalDateTime(TimeZone.getDefault())
                .toLocalDate()
                .plusDays(1)
        }

        fun new(
            nowTimestamp: Long,
        ): ChangeReminderEditor {
            val tomorrow = getTomorrow(nowTimestamp)
            return ChangeReminderEditor(
                id = 0,
                enabled = true,
                message = "",
                messageTouched = false,
                scheduleType = ScheduleType.ONE_TIME,
                daysOfWeek = DayOfWeek.entries.toSet(),
                date = tomorrow.toEpochDay(),
                dayOfMonth = tomorrow.dayOfMonth,
                timeOfDayMillis = TimeUnit.HOURS.toMillis(9),
                intervalSeconds = TimeUnit.HOURS.toSeconds(1),
                doNotDisturbStartMillis = 0L,
                doNotDisturbEndMillis = TimeUnit.HOURS.toMillis(8),
                conditionType = ConditionType.ALWAYS,
                conditionTarget = null,
            )
        }

        fun from(
            nowTimestamp: Long,
            reminder: ScheduledReminder,
        ): ChangeReminderEditor {
            val tomorrow = getTomorrow(nowTimestamp)
            val scheduleType: ScheduleType
            var daysOfWeek = DayOfWeek.entries.toSet()
            var date = tomorrow.toEpochDay()
            var dayOfMonth = tomorrow.dayOfMonth
            var intervalSeconds = TimeUnit.HOURS.toSeconds(1)
            var doNotDisturbStartMillis = 0L
            var doNotDisturbEndMillis = TimeUnit.HOURS.toMillis(8)

            when (val schedule = reminder.schedule) {
                is ScheduledReminder.Schedule.Weekly -> {
                    scheduleType = ScheduleType.WEEKLY
                    daysOfWeek = schedule.daysOfWeek
                }
                is ScheduledReminder.Schedule.OneTime -> {
                    scheduleType = ScheduleType.ONE_TIME
                    date = schedule.oneTimeDate
                }
                is ScheduledReminder.Schedule.Monthly -> {
                    scheduleType = ScheduleType.MONTHLY
                    dayOfMonth = schedule.dayOfMonth
                }
                is ScheduledReminder.Schedule.Hourly -> {
                    scheduleType = ScheduleType.HOURLY
                    daysOfWeek = schedule.daysOfWeek
                    date = schedule.startDate
                    intervalSeconds = schedule.intervalSeconds
                    doNotDisturbStartMillis = schedule.doNotDisturbStartMillis
                    doNotDisturbEndMillis = schedule.doNotDisturbEndMillis
                }
                is ScheduledReminder.Schedule.ActivityEvent -> {
                    scheduleType = when (schedule.event) {
                        RecordTimerEvent.STARTED -> ScheduleType.ACTIVITY_STARTED
                        RecordTimerEvent.STOPPED -> ScheduleType.ACTIVITY_STOPPED
                    }
                }
            }
            val condition = reminder.condition as? ScheduledReminder.Condition.RecordsNotTrackedToday
            val eventTarget = (reminder.schedule as? ScheduledReminder.Schedule.ActivityEvent)?.target
            return ChangeReminderEditor(
                id = reminder.id,
                enabled = reminder.enabled,
                message = reminder.text,
                messageTouched = true,
                scheduleType = scheduleType,
                daysOfWeek = daysOfWeek,
                date = date,
                dayOfMonth = dayOfMonth,
                timeOfDayMillis = when (val schedule = reminder.schedule) {
                    is ScheduledReminder.Schedule.Weekly -> schedule.timeOfDayMillis
                    is ScheduledReminder.Schedule.OneTime -> schedule.timeOfDayMillis
                    is ScheduledReminder.Schedule.Monthly -> schedule.timeOfDayMillis
                    is ScheduledReminder.Schedule.Hourly -> schedule.timeOfDayMillis
                    is ScheduledReminder.Schedule.ActivityEvent -> TimeUnit.HOURS.toMillis(9)
                },
                intervalSeconds = intervalSeconds,
                doNotDisturbStartMillis = doNotDisturbStartMillis,
                doNotDisturbEndMillis = doNotDisturbEndMillis,
                conditionType = if (condition == null) {
                    ConditionType.ALWAYS
                } else {
                    ConditionType.NOT_TRACKED
                },
                conditionTarget = eventTarget ?: condition?.target,
            )
        }
    }
}
