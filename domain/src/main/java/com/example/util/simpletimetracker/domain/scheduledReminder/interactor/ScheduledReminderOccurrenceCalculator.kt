package com.example.util.simpletimetracker.domain.scheduledReminder.interactor

import com.example.util.simpletimetracker.domain.extension.isValidTimeOfDay
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.toDomainDayOfWeek
import com.example.util.simpletimetracker.domain.extension.toLocalDateTime
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import java.util.TimeZone
import javax.inject.Inject

class ScheduledReminderOccurrenceCalculator @Inject constructor(
    private val localDateMapper: LocalDateMapper,
    private val getDoNotDisturbHandledScheduleInteractor: GetDoNotDisturbHandledScheduleInteractor,
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
            is ScheduledReminder.Schedule.Hourly -> calculateHourly(
                schedule = schedule,
                nowTimestamp = nowTimestamp,
                timeZone = timeZone,
            )
            is ScheduledReminder.Schedule.ActivityEvent -> null
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
                localDateMapper.resolveDateTime(
                    dateEpochDay = schedule.oneTimeDate,
                    timeOfDayMillis = schedule.timeOfDayMillis,
                    timeZone = timeZone,
                ) == expectedOccurrenceTimestamp
            }
            is ScheduledReminder.Schedule.Hourly,
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
            is ScheduledReminder.Schedule.ActivityEvent -> false
        }
    }

    private fun calculateHourly(
        schedule: ScheduledReminder.Schedule.Hourly,
        nowTimestamp: Long,
        timeZone: TimeZone,
    ): Occurrence? {
        if (schedule.daysOfWeek.isEmpty()) return null
        if (!schedule.timeOfDayMillis.isValidTimeOfDay()) return null
        if (!schedule.doNotDisturbStartMillis.isValidTimeOfDay()) return null
        if (!schedule.doNotDisturbEndMillis.isValidTimeOfDay()) return null
        if (schedule.intervalSeconds <= 0L) return null

        val intervalMillis = schedule.intervalSeconds * 1000L
        val startTimestamp = localDateMapper.resolveDateTime(
            dateEpochDay = schedule.startDate,
            timeOfDayMillis = schedule.timeOfDayMillis,
            timeZone = timeZone,
        ) ?: return null
        val firstGridOccurrence = startTimestamp + intervalMillis

        fun applyHourlyRestrictions(timestamp: Long): Long? {
            return getDoNotDisturbHandledScheduleInteractor.execute(
                timestamp = timestamp,
                dndStart = schedule.doNotDisturbStartMillis,
                dndEnd = schedule.doNotDisturbEndMillis,
                activeDaysOfWeek = schedule.daysOfWeek,
                timeZone = timeZone,
            )
        }

        val occurrence = if (firstGridOccurrence > nowTimestamp) {
            applyHourlyRestrictions(firstGridOccurrence)
        } else {
            val elapsedSinceStart = nowTimestamp - startTimestamp
            val gridOccurrenceAtOrBeforeNow = nowTimestamp - elapsedSinceStart % intervalMillis
            val restrictedCurrentOccurrence = applyHourlyRestrictions(gridOccurrenceAtOrBeforeNow)
            if (restrictedCurrentOccurrence != null && restrictedCurrentOccurrence > nowTimestamp) {
                restrictedCurrentOccurrence
            } else {
                applyHourlyRestrictions(gridOccurrenceAtOrBeforeNow + intervalMillis)
            }
        } ?: return null

        return Occurrence(
            triggerTimestamp = occurrence,
            expectedOccurrenceTimestamp = occurrence,
        )
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
                val timestamp = localDateMapper.resolveDateTime(
                    dateEpochDay = dateCursor.toEpochDay(),
                    timeOfDayMillis = schedule.timeOfDayMillis,
                    timeZone = timeZone,
                ).orZero()
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

        val expectedTimestamp = localDateMapper.resolveDateTime(
            dateEpochDay = schedule.oneTimeDate,
            timeOfDayMillis = schedule.timeOfDayMillis,
            timeZone = timeZone,
        ) ?: return null

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
            val timestamp = localDateMapper.resolveDateTime(
                dateEpochDay = date.toEpochDay(),
                timeOfDayMillis = schedule.timeOfDayMillis,
                timeZone = timeZone,
            ).orZero()
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

    data class Occurrence(
        val triggerTimestamp: Long,
        val expectedOccurrenceTimestamp: Long,
    )

    private companion object {
        const val DAYS_IN_WEEK = 7
    }
}
