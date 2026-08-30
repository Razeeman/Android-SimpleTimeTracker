package com.example.util.simpletimetracker.domain.scheduledReminder.interactor

import com.example.util.simpletimetracker.domain.extension.isValidTimeOfDay
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.toDomainDayOfWeek
import com.example.util.simpletimetracker.domain.extension.toLocalDateTime
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import java.time.LocalDate
import java.util.TimeZone
import javax.inject.Inject

class ScheduledReminderOccurrenceCalculator @Inject constructor(
    private val localDateMapper: LocalDateMapper,
) {

    /**
     * @param catchUpOverdueOneTime true - fire a missed one-time reminder immediately instead of dropping it as expired.
     */
    fun calculateNext(
        schedule: ScheduledReminder.Schedule,
        nowTimestamp: Long,
        timeZone: TimeZone,
        catchUpOverdueOneTime: Boolean,
    ): Occurrence? {
        return when (schedule) {
            is ScheduledReminder.Schedule.Weekly -> calculateWeekly(
                schedule = schedule,
                nowTimestamp = nowTimestamp,
                timeZone = timeZone,
            )
            is ScheduledReminder.Schedule.OneTime -> calculateOneTime(
                schedule = schedule,
                nowTimestamp = nowTimestamp,
                timeZone = timeZone,
                catchUpOverdue = catchUpOverdueOneTime,
            )
            is ScheduledReminder.Schedule.Monthly -> calculateMonthly(
                schedule = schedule,
                nowTimestamp = nowTimestamp,
                timeZone = timeZone,
            )
        }
    }

    /**
     * Checks whether an alarm's stored occurrence still belongs to the current schedule in the
     * current time zone, allowing stale alarms to be rejected after either one changes.
     */
    fun matchesExpectedOccurrence(
        schedule: ScheduledReminder.Schedule,
        expectedOccurrenceTimestamp: Long,
        timeZone: TimeZone,
    ): Boolean {
        if (expectedOccurrenceTimestamp == 0L) return false

        return when (schedule) {
            is ScheduledReminder.Schedule.OneTime -> {
                if (!schedule.timeOfDayMillis.isValidTimeOfDay()) return false
                resolveLocalDateTime(
                    dateEpochDay = schedule.oneTimeDate,
                    timeOfDayMillis = schedule.timeOfDayMillis,
                    timeZone = timeZone,
                ) == expectedOccurrenceTimestamp
            }
            is ScheduledReminder.Schedule.Weekly,
            is ScheduledReminder.Schedule.Monthly,
            -> calculateNext(
                schedule = schedule,
                // calculateNext returns only occurrences strictly after nowTimestamp. Starting one
                // millisecond before the candidate makes it eligible; starting at the candidate
                // would skip it and return the following recurrence instead.
                nowTimestamp = expectedOccurrenceTimestamp - 1L,
                timeZone = timeZone,
                catchUpOverdueOneTime = false,
            )?.expectedOccurrenceTimestamp == expectedOccurrenceTimestamp
        }
    }

    private fun calculateWeekly(
        schedule: ScheduledReminder.Schedule.Weekly,
        nowTimestamp: Long,
        timeZone: TimeZone,
    ): Occurrence? {
        if (schedule.daysOfWeek.isEmpty()) return null
        if (!schedule.timeOfDayMillis.isValidTimeOfDay()) return null

        var dateCursor = nowTimestamp.toLocalDateTime(timeZone).toLocalDate()

        // Include the same weekday next week in case today is selected but its time has passed.
        repeat(DAYS_IN_WEEK + 1) {
            val dayOfWeek = dateCursor.dayOfWeek.toDomainDayOfWeek()
            if (dayOfWeek in schedule.daysOfWeek) {
                val timestamp = resolveLocalDateTime(
                    dateEpochDay = dateCursor.toEpochDay(),
                    timeOfDayMillis = schedule.timeOfDayMillis,
                    timeZone = timeZone,
                )
                // Found next week day to schedule.
                if (timestamp > nowTimestamp) {
                    return Occurrence(
                        triggerTimestamp = timestamp,
                        expectedOccurrenceTimestamp = timestamp,
                    )
                }
            }
            dateCursor = dateCursor.plusDays(1)
        }

        return null
    }

    private fun calculateOneTime(
        schedule: ScheduledReminder.Schedule.OneTime,
        nowTimestamp: Long,
        timeZone: TimeZone,
        catchUpOverdue: Boolean,
    ): Occurrence? {
        if (!schedule.timeOfDayMillis.isValidTimeOfDay()) return null

        val expectedTimestamp = resolveLocalDateTime(
            dateEpochDay = schedule.oneTimeDate,
            timeOfDayMillis = schedule.timeOfDayMillis,
            timeZone = timeZone,
        )
        if (expectedTimestamp == 0L) return null

        return when {
            expectedTimestamp > nowTimestamp -> Occurrence(
                triggerTimestamp = expectedTimestamp,
                expectedOccurrenceTimestamp = expectedTimestamp,
            )
            // Show now.
            catchUpOverdue -> Occurrence(
                triggerTimestamp = nowTimestamp,
                expectedOccurrenceTimestamp = expectedTimestamp,
            )
            else -> null
        }
    }

    private fun calculateMonthly(
        schedule: ScheduledReminder.Schedule.Monthly,
        nowTimestamp: Long,
        timeZone: TimeZone,
    ): Occurrence? {
        if (schedule.dayOfMonth !in 1..31) return null
        if (!schedule.timeOfDayMillis.isValidTimeOfDay()) return null

        var monthCursor = nowTimestamp.toLocalDateTime(timeZone).toLocalDate().withDayOfMonth(1)

        // Check the current and following month. Since the day is clamped to the last valid day,
        // one of them always contains the next occurrence.
        repeat(2) {
            val date = monthCursor.withDayOfMonth(
                schedule.dayOfMonth.coerceAtMost(monthCursor.lengthOfMonth()),
            )
            val timestamp = resolveLocalDateTime(
                dateEpochDay = date.toEpochDay(),
                timeOfDayMillis = schedule.timeOfDayMillis,
                timeZone = timeZone,
            )
            // Found next month to schedule.
            if (timestamp > nowTimestamp) {
                return Occurrence(
                    triggerTimestamp = timestamp,
                    expectedOccurrenceTimestamp = timestamp,
                )
            }
            monthCursor = monthCursor.plusMonths(1)
        }

        return null
    }

    fun resolveLocalDateTime(
        dateEpochDay: Long,
        timeOfDayMillis: Long,
        timeZone: TimeZone,
    ): Long {
        val date = runCatching {
            LocalDate.ofEpochDay(dateEpochDay)
        }.getOrNull() ?: return 0L

        return localDateMapper.resolveDateTime(
            date = date,
            timeOfDayMillis = timeOfDayMillis,
            timeZone = timeZone,
        ).orZero()
    }

    data class Occurrence(
        val triggerTimestamp: Long,
        val expectedOccurrenceTimestamp: Long,
    )

    private companion object {
        const val DAYS_IN_WEEK = 7
    }
}
