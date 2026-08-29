package com.example.util.simpletimetracker.feature_notification.scheduledReminder.utils

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduledReminderOccurrenceCalculator @Inject constructor(
    private val timeMapper: TimeMapper,
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
                resolveLocalDateTime(
                    date = civilFromEpochDay(schedule.oneTimeDate),
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
        if (schedule.daysOfWeek.isEmpty() || !isValidTime(schedule.timeOfDayMillis)) return null

        val dateCursor = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowTimestamp
            setToStartOfDay()
            set(Calendar.HOUR_OF_DAY, 12)
        }

        // Include the same weekday next week in case today is selected but its time has passed.
        repeat(DAYS_IN_WEEK + 1) {
            val dayOfWeek = timeMapper.toDayOfWeek(dateCursor.get(Calendar.DAY_OF_WEEK))
            if (dayOfWeek in schedule.daysOfWeek) {
                val timestamp = resolveLocalDateTime(
                    date = dateCursor.toCivilDate(),
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
            dateCursor.add(Calendar.DATE, 1)
        }

        return null
    }

    private fun calculateOneTime(
        schedule: ScheduledReminder.Schedule.OneTime,
        nowTimestamp: Long,
        timeZone: TimeZone,
        catchUpOverdue: Boolean,
    ): Occurrence? {
        if (!isValidTime(schedule.timeOfDayMillis)) return null

        val expectedTimestamp = resolveLocalDateTime(
            date = civilFromEpochDay(schedule.oneTimeDate),
            timeOfDayMillis = schedule.timeOfDayMillis,
            timeZone = timeZone,
        )

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
        if (schedule.dayOfMonth !in 1..31 || !isValidTime(schedule.timeOfDayMillis)) return null

        val monthCursor = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowTimestamp
            set(Calendar.DAY_OF_MONTH, 1)
            setToStartOfDay()
            set(Calendar.HOUR_OF_DAY, 12)
        }

        // Check the current and following month. Since the day is clamped to the last valid day,
        // one of them always contains the next occurrence.
        repeat(2) {
            val date = CivilDate(
                year = monthCursor.get(Calendar.YEAR),
                month = monthCursor.get(Calendar.MONTH) + 1,
                day = schedule.dayOfMonth.coerceAtMost(
                    monthCursor.getActualMaximum(Calendar.DAY_OF_MONTH),
                ),
            )
            val timestamp = resolveLocalDateTime(
                date = date,
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
            monthCursor.add(Calendar.MONTH, 1)
        }

        return null
    }

    /**
     * Calendar's lenient resolution moves times in a spring gap forward by the gap. For an
     * overlap, Calendar implementations may choose either offset, so scan for the earliest
     * instant that maps to the requested local fields.
     */
    private fun resolveLocalDateTime(
        date: CivilDate,
        timeOfDayMillis: Long,
        timeZone: TimeZone,
    ): Long {
        val hour = TimeUnit.MILLISECONDS.toHours(timeOfDayMillis).toInt()
        val minute = TimeUnit.MILLISECONDS.toMinutes(timeOfDayMillis).toInt() % 60
        val second = TimeUnit.MILLISECONDS.toSeconds(timeOfDayMillis).toInt() % 60
        val millis = (timeOfDayMillis % 1000L).toInt()
        val calendar = Calendar.getInstance(timeZone).apply {
            // Leniency is enabled by default; keep it explicit because DST-gap normalization is intentional.
            isLenient = true
            clear()
            set(date.year, date.month - 1, date.day, hour, minute, second)
            set(Calendar.MILLISECOND, millis)
        }
        val resolved = calendar.timeInMillis

        // A gap no longer maps to the requested fields; Calendar's adjusted value is the policy.
        if (!calendar.matchesLocalDateTime(date, hour, minute, second, millis)) return resolved

        val scanCalendar = Calendar.getInstance(timeZone)
        var candidate = resolved - MAX_OVERLAP_SEARCH_MILLIS
        while (candidate < resolved) {
            scanCalendar.timeInMillis = candidate
            if (scanCalendar.matchesLocalDateTime(date, hour, minute, second, millis)) return candidate
            candidate += TimeUnit.MINUTES.toMillis(1)
        }
        return resolved
    }

    /**
     * Checks whether this instant maps back to the exact requested local date and time.
     * This detects normalization of nonexistent DST-gap times and identifies matching DST-overlap times.
     */
    private fun Calendar.matchesLocalDateTime(
        date: CivilDate,
        hour: Int,
        minute: Int,
        second: Int,
        millis: Int,
    ): Boolean {
        return get(Calendar.YEAR) == date.year &&
            get(Calendar.MONTH) + 1 == date.month &&
            get(Calendar.DAY_OF_MONTH) == date.day &&
            get(Calendar.HOUR_OF_DAY) == hour &&
            get(Calendar.MINUTE) == minute &&
            get(Calendar.SECOND) == second &&
            get(Calendar.MILLISECOND) == millis
    }

    private fun Calendar.toCivilDate(): CivilDate {
        return CivilDate(
            year = get(Calendar.YEAR),
            month = get(Calendar.MONTH) + 1,
            day = get(Calendar.DAY_OF_MONTH),
        )
    }

    private fun civilFromEpochDay(epochDay: Long): CivilDate {
        val date = LocalDate.ofEpochDay(epochDay)
        return CivilDate(
            year = date.year,
            month = date.monthValue,
            day = date.dayOfMonth,
        )
    }

    private fun isValidTime(timeOfDayMillis: Long): Boolean {
        return timeOfDayMillis in 0 until TimeUnit.DAYS.toMillis(1)
    }

    data class Occurrence(
        val triggerTimestamp: Long,
        val expectedOccurrenceTimestamp: Long,
    )

    private data class CivilDate(
        val year: Int,
        val month: Int,
        val day: Int,
    )

    private companion object {
        const val DAYS_IN_WEEK = 7

        // - Easily covers normal DST overlaps, usually one hour.
        // - Limits the scan to 360 minute-by-minute checks.
        // - Provides extra room for unusual multi-hour offset changes.
        //  However, the value is arbitrary and not universally sufficient. Oracle explicitly notes that
        //  transitions are usually one hour but can differ, and historical IANA data includes
        //  Pacific/Kwajalein moving from UTC+11 to UTC−12—a 23-hour overlap.
        val MAX_OVERLAP_SEARCH_MILLIS: Long = TimeUnit.HOURS.toMillis(6)
    }
}
