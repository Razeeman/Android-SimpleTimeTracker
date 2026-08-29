package com.example.util.simpletimetracker.feature_change_reminder.model

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderOccurrenceCalculator
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import java.time.Instant
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
    var oneTimeDate: Long,
    var dayOfMonth: Int,
    var timeOfDayMillis: Long,
    var conditionType: ConditionType,
    var activityId: Long?,
) {

    fun onMessageChanged(value: String) {
        message = value
        messageTouched = true
    }

    fun selectSchedule(type: ScheduleType) {
        scheduleType = type
        conditionType = ConditionType.ALWAYS
        activityId = null
    }

    fun selectCondition(type: ConditionType) {
        if (scheduleType != ScheduleType.WEEKLY) {
            conditionType = ConditionType.ALWAYS
            activityId = null
            return
        }
        conditionType = type
        if (type == ConditionType.ALWAYS) activityId = null
    }

    fun toggleDay(day: DayOfWeek) {
        daysOfWeek = daysOfWeek.addOrRemove(day)
    }

    fun selectActivity(type: RecordType, prefill: (String) -> String) {
        activityId = type.id
        if (!messageTouched || message.isBlank()) {
            message = prefill(type.name)
            messageTouched = false
        }
    }

    fun validate(
        nowTimestamp: Long,
        occurrenceCalculator: ScheduledReminderOccurrenceCalculator,
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
                val expectedTimestamp = occurrenceCalculator.resolveLocalDateTime(
                    dateEpochDay = oneTimeDate,
                    timeOfDayMillis = timeOfDayMillis,
                    timeZone = TimeZone.getDefault(),
                )
                if (expectedTimestamp <= nowTimestamp) {
                    return ValidationResult.Error(ValidationError.FUTURE_REQUIRED)
                }
                ScheduledReminder.Schedule.OneTime(oneTimeDate, timeOfDayMillis)
            }
            ScheduleType.MONTHLY -> {
                if (dayOfMonth !in 1..DAYS_IN_MONTH) dayOfMonth = 1
                ScheduledReminder.Schedule.Monthly(dayOfMonth, timeOfDayMillis)
            }
        }

        val condition = when {
            scheduleType != ScheduleType.WEEKLY -> ScheduledReminder.Condition.Always
            conditionType == ConditionType.NOT_TRACKED -> {
                activityId
                    ?.let { ScheduledReminder.Condition.ActivityNotTrackedToday(it) }
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

    enum class ScheduleType { WEEKLY, ONE_TIME, MONTHLY }
    enum class ConditionType { ALWAYS, NOT_TRACKED }
    enum class ValidationError { MESSAGE_REQUIRED, FUTURE_REQUIRED }

    sealed interface ValidationResult {
        data class Valid(val reminder: ScheduledReminder) : ValidationResult
        data class Error(val error: ValidationError) : ValidationResult
    }

    companion object {
        const val DAYS_IN_MONTH = 31

        private fun getTomorrow(nowTimestamp: Long): LocalDate {
            return Instant.ofEpochMilli(nowTimestamp)
                .atZone(TimeZone.getDefault().toZoneId()).toLocalDate()
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
                scheduleType = ScheduleType.WEEKLY,
                daysOfWeek = DayOfWeek.entries.toSet(),
                oneTimeDate = tomorrow.toEpochDay(),
                dayOfMonth = tomorrow.dayOfMonth,
                timeOfDayMillis = TimeUnit.HOURS.toMillis(9),
                conditionType = ConditionType.ALWAYS,
                activityId = null,
            )
        }

        fun from(
            nowTimestamp: Long,
            reminder: ScheduledReminder,
        ): ChangeReminderEditor {
            val tomorrow = getTomorrow(nowTimestamp)
            val scheduleType: ScheduleType
            var daysOfWeek = DayOfWeek.entries.toSet()
            var oneTimeDate = tomorrow.toEpochDay()
            var dayOfMonth = tomorrow.dayOfMonth

            when (val schedule = reminder.schedule) {
                is ScheduledReminder.Schedule.Weekly -> {
                    scheduleType = ScheduleType.WEEKLY
                    daysOfWeek = schedule.daysOfWeek
                }
                is ScheduledReminder.Schedule.OneTime -> {
                    scheduleType = ScheduleType.ONE_TIME
                    oneTimeDate = schedule.oneTimeDate
                }
                is ScheduledReminder.Schedule.Monthly -> {
                    scheduleType = ScheduleType.MONTHLY
                    dayOfMonth = schedule.dayOfMonth
                }
            }
            val condition = reminder.condition as? ScheduledReminder.Condition.ActivityNotTrackedToday
            return ChangeReminderEditor(
                id = reminder.id,
                enabled = reminder.enabled,
                message = reminder.text,
                messageTouched = true,
                scheduleType = scheduleType,
                daysOfWeek = daysOfWeek,
                oneTimeDate = oneTimeDate,
                dayOfMonth = dayOfMonth,
                timeOfDayMillis = reminder.schedule.timeOfDayMillis,
                conditionType = if (condition == null) {
                    ConditionType.ALWAYS
                } else {
                    ConditionType.NOT_TRACKED
                },
                activityId = condition?.activityId,
            )
        }
    }
}
