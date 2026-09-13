package com.example.util.simpletimetracker.domain.scheduledReminder.interactor

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class ScheduledReminderOccurrenceCalculatorTest {

    private val localDateMapper = LocalDateMapper()
    private val subject = ScheduledReminderOccurrenceCalculator(
        localDateMapper = localDateMapper,
        getDoNotDisturbHandledScheduleInteractor = GetDoNotDisturbHandledScheduleInteractor(
            localDateMapper = localDateMapper,
        ),
    )

    @Test
    fun `one-time reminder in DST gap is moved forward by the gap`() {
        val actual = subject.calculateNext(
            schedule = oneTimeSchedule(
                date = LocalDate.of(2025, 3, 30),
                hour = 2,
                minute = 30,
            ),
            nowTimestamp = timestamp("2025-03-29T00:00:00Z"),
            timeZone = TimeZone.getTimeZone("Europe/Berlin"),
            catchUpOverdueOneTime = false,
        )

        val expectedTimestamp = timestamp("2025-03-30T01:30:00Z")
        assertEquals(
            ScheduledReminderOccurrenceCalculator.Occurrence(
                triggerTimestamp = expectedTimestamp,
                expectedOccurrenceTimestamp = expectedTimestamp,
            ),
            actual,
        )
    }

    @Test
    fun `one-time reminder in DST overlap uses the earlier occurrence`() {
        val actual = subject.calculateNext(
            schedule = oneTimeSchedule(
                date = LocalDate.of(2025, 10, 26),
                hour = 2,
                minute = 30,
            ),
            nowTimestamp = timestamp("2025-10-25T00:00:00Z"),
            timeZone = TimeZone.getTimeZone("Europe/Berlin"),
            catchUpOverdueOneTime = false,
        )

        val expectedTimestamp = timestamp("2025-10-26T00:30:00Z")
        assertEquals(
            ScheduledReminderOccurrenceCalculator.Occurrence(
                triggerTimestamp = expectedTimestamp,
                expectedOccurrenceTimestamp = expectedTimestamp,
            ),
            actual,
        )
    }

    @Test
    fun `weekly reminder whose time passed today is scheduled next week`() {
        val actual = subject.calculateNext(
            schedule = ScheduledReminder.Schedule.Weekly(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                timeOfDayMillis = timeOfDayMillis(hour = 9),
            ),
            nowTimestamp = timestamp("2025-01-06T10:00:00Z"),
            timeZone = TimeZone.getTimeZone("UTC"),
            catchUpOverdueOneTime = false,
        )

        val expectedTimestamp = timestamp("2025-01-13T09:00:00Z")
        assertEquals(
            ScheduledReminderOccurrenceCalculator.Occurrence(
                triggerTimestamp = expectedTimestamp,
                expectedOccurrenceTimestamp = expectedTimestamp,
            ),
            actual,
        )
    }

    @Test
    fun `monthly reminder day is clamped to the end of a shorter month`() {
        val actual = subject.calculateNext(
            schedule = ScheduledReminder.Schedule.Monthly(
                dayOfMonth = 31,
                timeOfDayMillis = timeOfDayMillis(hour = 9),
            ),
            nowTimestamp = timestamp("2025-02-01T00:00:00Z"),
            timeZone = TimeZone.getTimeZone("UTC"),
            catchUpOverdueOneTime = false,
        )

        val expectedTimestamp = timestamp("2025-02-28T09:00:00Z")
        assertEquals(
            ScheduledReminderOccurrenceCalculator.Occurrence(
                triggerTimestamp = expectedTimestamp,
                expectedOccurrenceTimestamp = expectedTimestamp,
            ),
            actual,
        )
    }

    @Test
    fun `invalid one-time date does not throw while calculating occurrence`() {
        val schedule = ScheduledReminder.Schedule.OneTime(
            oneTimeDate = Long.MAX_VALUE,
            timeOfDayMillis = timeOfDayMillis(hour = 9),
        )

        assertNull(
            subject.calculateNext(
                schedule = schedule,
                nowTimestamp = timestamp("2025-01-01T00:00:00Z"),
                timeZone = TimeZone.getTimeZone("UTC"),
                catchUpOverdueOneTime = false,
            ),
        )
        assertFalse(
            subject.matchesExpectedOccurrence(
                schedule = schedule,
                expectedOccurrenceTimestamp = timestamp("2025-01-02T09:00:00Z"),
                timeZone = TimeZone.getTimeZone("UTC"),
            ),
        )
    }

    @Test
    fun `date mapper returns null for invalid inputs`() {
        assertNull(
            localDateMapper.resolveDateTime(
                dateEpochDay = Long.MAX_VALUE,
                timeOfDayMillis = timeOfDayMillis(hour = 9),
                timeZone = TimeZone.getTimeZone("UTC"),
            ),
        )
        assertNull(
            localDateMapper.resolveDateTime(
                dateEpochDay = LocalDate.of(2025, 1, 1).toEpochDay(),
                timeOfDayMillis = TimeUnit.DAYS.toMillis(1),
                timeZone = TimeZone.getTimeZone("UTC"),
            ),
        )
    }

    @Test
    fun `hourly first reminder is one interval after counting start`() {
        val actual = calculateHourly(
            schedule = hourlySchedule(start = "2025-01-06T10:00:00Z", intervalMinutes = 60),
            now = "2025-01-06T10:00:00Z",
        )

        assertOccurrence("2025-01-06T11:00:00Z", actual)
    }

    @Test
    fun `fifteen minute hourly schedule keeps its sequence`() {
        val schedule = hourlySchedule(start = "2025-01-06T10:00:00Z", intervalMinutes = 15)
        val first = calculateHourly(schedule, "2025-01-06T10:00:00Z")
        val second = calculateHourly(
            schedule = schedule,
            now = "2025-01-06T10:15:00Z",
        )

        assertOccurrence("2025-01-06T10:15:00Z", first)
        assertOccurrence("2025-01-06T10:30:00Z", second)
    }

    @Test
    fun `five hour schedule continues across midnight`() {
        val schedule = hourlySchedule(start = "2025-01-06T20:00:00Z", intervalMinutes = 300)
        val first = calculateHourly(schedule, "2025-01-06T20:00:00Z")
        val second = calculateHourly(
            schedule = schedule,
            now = "2025-01-07T01:00:00Z",
        )

        assertOccurrence("2025-01-07T01:00:00Z", first)
        assertOccurrence("2025-01-07T06:00:00Z", second)
    }

    @Test
    fun `inactive day defers to midnight on next active day`() {
        val schedule = hourlySchedule(
            start = "2025-01-10T23:00:00Z",
            intervalMinutes = 120,
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
        )

        assertOccurrence(
            "2025-01-13T00:00:00Z",
            calculateHourly(schedule, "2025-01-10T23:00:00Z"),
        )
    }

    @Test
    fun `inactive day defers delivery without shifting interval grid`() {
        val schedule = hourlySchedule(
            start = "2025-01-10T23:00:00Z",
            intervalMinutes = 120,
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
        )

        assertOccurrence(
            "2025-01-13T01:00:00Z",
            calculateHourly(schedule, "2025-01-13T00:00:00Z"),
        )
    }

    @Test
    fun `inactive day deferral applies dnd at midnight`() {
        val schedule = hourlySchedule(
            start = "2025-01-10T23:00:00Z",
            intervalMinutes = 120,
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            dndStartHour = 0,
            dndEndHour = 8,
        )

        assertOccurrence(
            "2025-01-13T08:00:00Z",
            calculateHourly(schedule, "2025-01-10T23:00:00Z"),
        )
    }

    @Test
    fun `same day dnd defers delivery without shifting interval grid`() {
        val schedule = hourlySchedule(
            start = "2025-01-06T08:00:00Z",
            intervalMinutes = 90,
            dndStartHour = 9,
            dndEndHour = 12,
        )
        val first = calculateHourly(schedule, "2025-01-06T08:00:00Z")
        val second = calculateHourly(
            schedule,
            now = "2025-01-06T12:00:00Z",
        )

        assertOccurrence("2025-01-06T12:00:00Z", first)
        assertOccurrence("2025-01-06T12:30:00Z", second)
    }

    @Test
    fun `overnight dnd defers occurrence to next morning`() {
        val schedule = hourlySchedule(
            start = "2025-01-06T21:00:00Z",
            intervalMinutes = 60,
            dndStartHour = 22,
            dndEndHour = 6,
        )

        assertOccurrence(
            "2025-01-07T06:00:00Z",
            calculateHourly(schedule, "2025-01-06T21:00:00Z"),
        )
    }

    @Test
    fun `dnd is start inclusive and end exclusive`() {
        val atStart = hourlySchedule(
            start = "2025-01-06T08:00:00Z",
            intervalMinutes = 60,
            dndStartHour = 9,
            dndEndHour = 12,
        )
        val atEnd = hourlySchedule(
            start = "2025-01-06T11:00:00Z",
            intervalMinutes = 60,
            dndStartHour = 9,
            dndEndHour = 12,
        )

        assertOccurrence("2025-01-06T12:00:00Z", calculateHourly(atStart, "2025-01-06T08:00:00Z"))
        assertOccurrence("2025-01-06T12:00:00Z", calculateHourly(atEnd, "2025-01-06T11:00:00Z"))
    }

    @Test
    fun `equal dnd times disable dnd`() {
        val schedule = hourlySchedule(
            start = "2025-01-06T08:00:00Z",
            intervalMinutes = 60,
            dndStartHour = 9,
            dndEndHour = 9,
        )

        assertOccurrence("2025-01-06T09:00:00Z", calculateHourly(schedule, "2025-01-06T08:00:00Z"))
    }

    @Test
    fun `delayed hourly alarm advances on configured interval grid`() {
        val schedule = hourlySchedule(start = "2025-01-06T10:00:00Z", intervalMinutes = 60)

        assertOccurrence(
            "2025-01-06T14:00:00Z",
            calculateHourly(
                schedule = schedule,
                now = "2025-01-06T13:25:00Z",
            ),
        )
    }

    @Test
    fun `missed intervals are skipped without catch up`() {
        val schedule = hourlySchedule(start = "2025-01-06T10:00:00Z", intervalMinutes = 15)

        assertOccurrence(
            "2025-01-06T12:15:00Z",
            calculateHourly(schedule, "2025-01-06T12:07:00Z"),
        )
    }

    @Test
    fun `hourly expected occurrence is derived from configured interval grid`() {
        val schedule = hourlySchedule(start = "2025-01-06T10:00:00Z", intervalMinutes = 60)
        val expected = timestamp("2025-01-06T11:00:00Z")

        assertTrue(
            subject.matchesExpectedOccurrence(
                schedule = schedule,
                expectedOccurrenceTimestamp = expected,
                timeZone = TimeZone.getTimeZone("UTC"),
            ),
        )
        assertFalse(
            subject.matchesExpectedOccurrence(
                schedule = schedule,
                expectedOccurrenceTimestamp = timestamp("2025-01-06T11:30:00Z"),
                timeZone = TimeZone.getTimeZone("UTC"),
            ),
        )
    }

    @Test
    fun `hourly duration remains continuous across DST gap`() {
        val schedule = hourlySchedule(
            start = "2025-03-29T11:00:00Z",
            intervalMinutes = 24 * 60L,
            timeZone = TimeZone.getTimeZone("Europe/Berlin"),
        )

        assertOccurrence(
            "2025-03-30T11:00:00Z",
            calculateHourly(
                schedule = schedule,
                now = "2025-03-29T11:00:00Z",
                timeZone = TimeZone.getTimeZone("Europe/Berlin"),
            ),
        )
    }

    private fun oneTimeSchedule(
        date: LocalDate,
        hour: Int,
        minute: Int,
    ): ScheduledReminder.Schedule.OneTime {
        return ScheduledReminder.Schedule.OneTime(
            oneTimeDate = date.toEpochDay(),
            timeOfDayMillis = timeOfDayMillis(hour, minute),
        )
    }

    private fun timeOfDayMillis(hour: Int, minute: Int = 0): Long {
        return TimeUnit.HOURS.toMillis(hour.toLong()) +
            TimeUnit.MINUTES.toMillis(minute.toLong())
    }

    private fun hourlySchedule(
        start: String,
        intervalMinutes: Long,
        days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        dndStartHour: Int = 0,
        dndEndHour: Int = 0,
        timeZone: TimeZone = TimeZone.getTimeZone("UTC"),
    ): ScheduledReminder.Schedule.Hourly {
        val dateTime = timestamp(start).let { Instant.ofEpochMilli(it).atZone(timeZone.toZoneId()) }
        return ScheduledReminder.Schedule.Hourly(
            intervalSeconds = TimeUnit.MINUTES.toSeconds(intervalMinutes),
            startDate = dateTime.toLocalDate().toEpochDay(),
            timeOfDayMillis = timeOfDayMillis(dateTime.hour, dateTime.minute),
            daysOfWeek = days,
            doNotDisturbStartMillis = timeOfDayMillis(dndStartHour),
            doNotDisturbEndMillis = timeOfDayMillis(dndEndHour),
        )
    }

    private fun calculateHourly(
        schedule: ScheduledReminder.Schedule.Hourly,
        now: String,
        timeZone: TimeZone = TimeZone.getTimeZone("UTC"),
    ): ScheduledReminderOccurrenceCalculator.Occurrence? {
        return subject.calculateNext(
            schedule = schedule,
            nowTimestamp = timestamp(now),
            timeZone = timeZone,
            catchUpOverdueOneTime = false,
        )
    }

    private fun assertOccurrence(
        expected: String,
        actual: ScheduledReminderOccurrenceCalculator.Occurrence?,
    ) {
        val expectedTimestamp = timestamp(expected)
        assertEquals(
            ScheduledReminderOccurrenceCalculator.Occurrence(
                triggerTimestamp = expectedTimestamp,
                expectedOccurrenceTimestamp = expectedTimestamp,
            ),
            actual,
        )
    }

    private fun timestamp(value: String): Long = Instant.parse(value).toEpochMilli()
}
