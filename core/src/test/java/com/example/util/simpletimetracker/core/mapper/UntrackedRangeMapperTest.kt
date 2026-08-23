package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.model.Range
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class UntrackedRangeMapperTest {

    private lateinit var subject: UntrackedRangeMapper
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        subject = UntrackedRangeMapper(
            TimeMapper(
                localeProvider = mock(LocaleProvider::class.java),
                resourceRepo = mock(BaseResourceRepo::class.java),
                currentTimestampProvider = mock(CurrentTimestampProvider::class.java),
            ),
        )
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `all days preserve range when time range is disabled`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1, 6), timestamp(2, 6))),
            daysOfWeek = DayOfWeek.entries.toSet(),
            timeOfDay = null,
        )

        assertEquals(
            listOf(Range(timestamp(1, 6), timestamp(2, 6))),
            result,
        )
    }

    @Test
    fun `only selected day is included from a multi day gap`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1, 12), timestamp(3, 12))),
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            timeOfDay = null,
        )

        assertEquals(listOf(Range(timestamp(1, 12), timestamp(2))), result)
    }

    @Test
    fun `no selected days returns no ranges`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1), timestamp(3))),
            daysOfWeek = emptySet(),
            timeOfDay = null,
        )

        assertEquals(emptyList<Range>(), result)
    }

    @Test
    fun `daytime range is applied only to selected days`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1), timestamp(3))),
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            timeOfDay = Range(hours(8), hours(17)),
        )

        assertEquals(listOf(Range(timestamp(1, 8), timestamp(1, 17))), result)
    }

    @Test
    fun `overnight range includes only physical portions of selected day`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1), timestamp(3))),
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            timeOfDay = Range(hours(17), hours(8)),
        )

        assertEquals(
            listOf(
                Range(timestamp(1), timestamp(1, 8)),
                Range(timestamp(1, 17), timestamp(2)),
            ),
            result,
        )
    }

    @Test
    fun `equal range endpoints leave selected day unrestricted`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1, 4), timestamp(2, 4))),
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            timeOfDay = Range(hours(8), hours(8)),
        )

        assertEquals(listOf(Range(timestamp(1, 4), timestamp(2))), result)
    }

    @Test
    fun `time of day processing includes first window when it reaches record end`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1, 10), timestamp(1, 12))),
            daysOfWeek = DayOfWeek.entries.toSet(),
            timeOfDay = Range(hours(8), hours(17)),
        )

        assertEquals(listOf(Range(timestamp(1, 10), timestamp(1, 12))), result)
    }

    @Test
    fun `time of day processing advances until window reaches record end`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1, 12), timestamp(3, 12))),
            daysOfWeek = DayOfWeek.entries.toSet(),
            timeOfDay = Range(hours(8), hours(17)),
        )

        assertEquals(
            listOf(
                Range(timestamp(1, 12), timestamp(1, 17)),
                Range(timestamp(2, 8), timestamp(2, 17)),
                Range(timestamp(3, 8), timestamp(3, 12)),
            ),
            result,
        )
    }

    @Test
    fun `time of day processing applies days of week when time range is disabled`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1, 12), timestamp(3, 12))),
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            timeOfDay = null,
        )

        assertEquals(listOf(Range(timestamp(1, 12), timestamp(2))), result)
    }

    @Test
    fun `overnight time range remains continuous when all days are selected`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1, 12), timestamp(2, 12))),
            daysOfWeek = DayOfWeek.entries.toSet(),
            timeOfDay = Range(hours(17), hours(8)),
        )

        assertEquals(
            listOf(Range(timestamp(1, 17), timestamp(2, 8))),
            result,
        )
    }

    @Test
    fun `overnight time range remains continuous across adjacent selected days`() {
        val result = subject.processSettings(
            ranges = listOf(Range(timestamp(1, 12), timestamp(2, 12))),
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            timeOfDay = Range(hours(17), hours(8)),
        )

        assertEquals(
            listOf(Range(timestamp(1, 17), timestamp(2, 8))),
            result,
        )
    }

    @Test
    fun `time of day uses local wall clock across daylight saving changes`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))

        val result = subject.processSettings(
            ranges = listOf(
                Range(
                    dateTimestamp(2024, Calendar.MARCH, 31),
                    dateTimestamp(2024, Calendar.APRIL, 1),
                ),
                Range(
                    dateTimestamp(2024, Calendar.OCTOBER, 27),
                    dateTimestamp(2024, Calendar.OCTOBER, 28),
                ),
            ),
            daysOfWeek = DayOfWeek.entries.toSet(),
            timeOfDay = Range(hours(8), hours(17)),
        )

        assertEquals(
            listOf(
                Range(
                    dateTimestamp(2024, Calendar.MARCH, 31, 8),
                    dateTimestamp(2024, Calendar.MARCH, 31, 17),
                ),
                Range(
                    dateTimestamp(2024, Calendar.OCTOBER, 27, 8),
                    dateTimestamp(2024, Calendar.OCTOBER, 27, 17),
                ),
            ),
            result,
        )
    }

    private fun timestamp(
        day: Int,
        hour: Int = 0,
    ): Long {
        return Calendar.getInstance().apply {
            clear()
            set(2024, Calendar.JANUARY, day, hour, 0, 0)
        }.timeInMillis
    }

    private fun dateTimestamp(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 0,
    ): Long {
        return Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, 0, 0)
        }.timeInMillis
    }

    private fun hours(value: Long): Long = TimeUnit.HOURS.toMillis(value)
}
