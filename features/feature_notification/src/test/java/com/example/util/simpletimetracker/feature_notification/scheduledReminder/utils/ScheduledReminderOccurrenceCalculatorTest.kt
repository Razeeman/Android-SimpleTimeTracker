package com.example.util.simpletimetracker.feature_notification.scheduledReminder.utils

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.time.Instant
import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class ScheduledReminderOccurrenceCalculatorTest {

    private val timeMapper: TimeMapper = Mockito.mock(TimeMapper::class.java)
    private val subject = ScheduledReminderOccurrenceCalculator(timeMapper)

    @Before
    fun setUp() {
        Mockito.`when`(timeMapper.toDayOfWeek(Calendar.SUNDAY)).thenReturn(DayOfWeek.SUNDAY)
        Mockito.`when`(timeMapper.toDayOfWeek(Calendar.MONDAY)).thenReturn(DayOfWeek.MONDAY)
        Mockito.`when`(timeMapper.toDayOfWeek(Calendar.TUESDAY)).thenReturn(DayOfWeek.TUESDAY)
        Mockito.`when`(timeMapper.toDayOfWeek(Calendar.WEDNESDAY)).thenReturn(DayOfWeek.WEDNESDAY)
        Mockito.`when`(timeMapper.toDayOfWeek(Calendar.THURSDAY)).thenReturn(DayOfWeek.THURSDAY)
        Mockito.`when`(timeMapper.toDayOfWeek(Calendar.FRIDAY)).thenReturn(DayOfWeek.FRIDAY)
        Mockito.`when`(timeMapper.toDayOfWeek(Calendar.SATURDAY)).thenReturn(DayOfWeek.SATURDAY)
    }

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

    private fun timestamp(value: String): Long = Instant.parse(value).toEpochMilli()
}
