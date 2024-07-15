package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.TimeMapperTitleTest.Subject.currentTimestampProvider
import com.example.util.simpletimetracker.core.mapper.TimeMapperTitleTest.Subject.hourInMs
import com.example.util.simpletimetracker.core.mapper.TimeMapperTitleTest.Subject.localeProvider
import com.example.util.simpletimetracker.core.mapper.TimeMapperTitleTest.Subject.resourceRepo
import com.example.util.simpletimetracker.domain.provider.CurrentTimestampProvider
import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@RunWith(Enclosed::class)
class TimeMapperTitleTest {

    private object Subject {
        val resourceRepo: ResourceRepo = Mockito.mock(ResourceRepo::class.java)
        val currentTimestampProvider: CurrentTimestampProvider = Mockito.mock(CurrentTimestampProvider::class.java)
        val localeProvider: LocaleProvider = Mockito.mock(LocaleProvider::class.java)
        val hourInMs = TimeUnit.HOURS.toMillis(1)
    }

    @RunWith(Parameterized::class)
    class ToDayTitleTest(
        private val input: List<Any>,
        private val output: String,
    ) {

        @Before
        fun before() {
            `when`(resourceRepo.getString(R.string.title_yesterday)).thenReturn("yesterday")
            `when`(resourceRepo.getString(R.string.title_today)).thenReturn("today")
            `when`(resourceRepo.getString(R.string.title_tomorrow)).thenReturn("tomorrow")
        }

        @Test
        fun toDayTitle() {
            `when`(currentTimestampProvider.get()).thenReturn(input[2] as Long)
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(localeProvider, resourceRepo, currentTimestampProvider)

            assertEquals(
                "Test failed for params $input",
                output,
                subject.toDayTitle(
                    daysFromToday = input[0] as Int,
                    startOfDayShift = input[1] as Long,
                ),
            )
        }

        companion object {
            private val localeDefault: Locale = Locale.getDefault()
            private val timezoneDefault: TimeZone = TimeZone.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                Locale.setDefault(Locale.US)
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                Locale.setDefault(localeDefault)
                TimeZone.setDefault(timezoneDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // days from today, start of day shift, current timestamp in ms

                // default
                arrayOf(listOf<Any>(-1, 0L, 0L), "yesterday"),
                arrayOf(listOf<Any>(-1, 0L, 24 * hourInMs - 1), "yesterday"),
                arrayOf(listOf<Any>(0, 0L, 0L), "today"),
                arrayOf(listOf<Any>(0, 0L, 24 * hourInMs - 1), "today"),
                arrayOf(listOf<Any>(1, 0L, 0L), "tomorrow"),
                arrayOf(listOf<Any>(1, 0L, 24 * hourInMs - 1), "tomorrow"),

                // days from today
                arrayOf(listOf<Any>(-2, 0L, 0L), "Tue, Dec 30"),
                arrayOf(listOf<Any>(-2, 0L, 24 * hourInMs - 1), "Tue, Dec 30"),
                arrayOf(listOf<Any>(2, 0L, 0L), "Sat, Jan 3"),
                arrayOf(listOf<Any>(2, 0L, 24 * hourInMs - 1), "Sat, Jan 3"),

                // negative start of day
                arrayOf(listOf<Any>(-2, -hourInMs, -hourInMs - 1), "Mon, Dec 29"),
                arrayOf(listOf<Any>(-2, -hourInMs, -hourInMs + 1), "Tue, Dec 30"),

                // positive start of day
                arrayOf(listOf<Any>(-2, hourInMs, hourInMs - 1), "Mon, Dec 29"),
                arrayOf(listOf<Any>(-2, hourInMs, hourInMs + 1), "Tue, Dec 30"),
            )
        }
    }

    @RunWith(Parameterized::class)
    class ToWeekTitleTest(
        private val input: List<Any>,
        private val output: String,
    ) {

        @Before
        fun before() {
            `when`(resourceRepo.getString(R.string.title_this_week)).thenReturn("this week")
        }

        @Test
        fun toWeekTitle() {
            `when`(currentTimestampProvider.get()).thenReturn(input[3] as Long)
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(localeProvider, resourceRepo, currentTimestampProvider)

            assertEquals(
                "Test failed for params $input",
                output,
                subject.toWeekTitle(
                    weeksFromToday = input[0] as Int,
                    startOfDayShift = input[1] as Long,
                    firstDayOfWeek = input[2] as DayOfWeek,
                ),
            )
        }

        companion object {
            private val localeDefault: Locale = Locale.getDefault()
            private val timezoneDefault: TimeZone = TimeZone.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                Locale.setDefault(Locale.US)
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                Locale.setDefault(localeDefault)
                TimeZone.setDefault(timezoneDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // weeks from today, start of day shift, first day of week, current timestamp in ms

                // default
                arrayOf(listOf<Any>(0, 0L, DayOfWeek.MONDAY, 0L), "this week"),
                arrayOf(listOf<Any>(0, 0L, DayOfWeek.MONDAY, 24 * hourInMs - 1), "this week"),

                // weeks from today
                arrayOf(listOf<Any>(-3, 0L, DayOfWeek.MONDAY, 0L), "Dec 8 - Dec 14"),
                arrayOf(listOf<Any>(-2, 0L, DayOfWeek.MONDAY, 0L), "Dec 15 - Dec 21"),
                arrayOf(listOf<Any>(-1, 0L, DayOfWeek.MONDAY, 0L), "Dec 22 - Dec 28"),
                arrayOf(listOf<Any>(1, 0L, DayOfWeek.MONDAY, 0L), "Jan 5 - Jan 11"),
                arrayOf(listOf<Any>(2, 0L, DayOfWeek.MONDAY, 0L), "Jan 12 - Jan 18"),
                arrayOf(listOf<Any>(3, 0L, DayOfWeek.MONDAY, 0L), "Jan 19 - Jan 25"),

                // from 2022.01.18
                arrayOf(listOf<Any>(-50, 0L, DayOfWeek.MONDAY, 1642506258000L), "Feb 1 - Feb 7"),
                arrayOf(listOf<Any>(50, 0L, DayOfWeek.MONDAY, 1642506258000L), "Jan 2 - Jan 8"),

                // first day of week
                arrayOf(listOf<Any>(-1, 0L, DayOfWeek.MONDAY, 0L), "Dec 22 - Dec 28"),
                arrayOf(listOf<Any>(1, 0L, DayOfWeek.MONDAY, 0L), "Jan 5 - Jan 11"),

                arrayOf(listOf<Any>(-1, 0L, DayOfWeek.TUESDAY, 0L), "Dec 23 - Dec 29"),
                arrayOf(listOf<Any>(1, 0L, DayOfWeek.TUESDAY, 0L), "Jan 6 - Jan 12"),

                arrayOf(listOf<Any>(-1, 0L, DayOfWeek.WEDNESDAY, 0L), "Dec 24 - Dec 30"),
                arrayOf(listOf<Any>(1, 0L, DayOfWeek.WEDNESDAY, 0L), "Jan 7 - Jan 13"),

                arrayOf(listOf<Any>(-1, 0L, DayOfWeek.THURSDAY, 0L), "Dec 25 - Dec 31"),
                arrayOf(listOf<Any>(1, 0L, DayOfWeek.THURSDAY, 0L), "Jan 8 - Jan 14"),

                arrayOf(listOf<Any>(-1, 0L, DayOfWeek.FRIDAY, 0L), "Dec 19 - Dec 25"),
                arrayOf(listOf<Any>(1, 0L, DayOfWeek.FRIDAY, 0L), "Jan 2 - Jan 8"),

                arrayOf(listOf<Any>(-1, 0L, DayOfWeek.SATURDAY, 0L), "Dec 20 - Dec 26"),
                arrayOf(listOf<Any>(1, 0L, DayOfWeek.SATURDAY, 0L), "Jan 3 - Jan 9"),

                arrayOf(listOf<Any>(-1, 0L, DayOfWeek.SUNDAY, 0L), "Dec 21 - Dec 27"),
                arrayOf(listOf<Any>(1, 0L, DayOfWeek.SUNDAY, 0L), "Jan 4 - Jan 10"),

                // negative start of day, middle of week
                arrayOf(listOf<Any>(1, -hourInMs, DayOfWeek.MONDAY, -hourInMs - 1), "Jan 5 - Jan 11"),
                arrayOf(listOf<Any>(1, -hourInMs, DayOfWeek.MONDAY, -hourInMs + 1), "Jan 5 - Jan 11"),

                // negative start of day, start of week 1970.01.05
                arrayOf(listOf<Any>(1, -hourInMs, DayOfWeek.MONDAY, 345600000L - hourInMs - 1), "Jan 5 - Jan 11"),
                arrayOf(listOf<Any>(1, -hourInMs, DayOfWeek.MONDAY, 345600000L - hourInMs + 1), "Jan 12 - Jan 18"),

                // negative start of day, first day of week changed to start of week
                arrayOf(listOf<Any>(1, -hourInMs, DayOfWeek.THURSDAY, -hourInMs - 1), "Jan 1 - Jan 7"),
                arrayOf(listOf<Any>(1, -hourInMs, DayOfWeek.THURSDAY, -hourInMs + 1), "Jan 8 - Jan 14"),

                // negative start of day, end of week 1970.01.11
                arrayOf(listOf<Any>(1, -hourInMs, DayOfWeek.MONDAY, 864000000L + 23 * hourInMs - 1), "Jan 12 - Jan 18"),
                arrayOf(listOf<Any>(1, -hourInMs, DayOfWeek.MONDAY, 864000000L + 23 * hourInMs + 1), "Jan 19 - Jan 25"),

                // negative start of day, first day of week changed to end of week
                arrayOf(listOf<Any>(1, -hourInMs, DayOfWeek.FRIDAY, 23 * hourInMs - 1), "Jan 2 - Jan 8"),
                arrayOf(listOf<Any>(1, -hourInMs, DayOfWeek.FRIDAY, 23 * hourInMs + 1), "Jan 9 - Jan 15"),

                // positive start of day, middle of week
                arrayOf(listOf<Any>(1, hourInMs, DayOfWeek.MONDAY, hourInMs - 1), "Jan 5 - Jan 11"),
                arrayOf(listOf<Any>(1, hourInMs, DayOfWeek.MONDAY, hourInMs + 1), "Jan 5 - Jan 11"),

                // positive start of day, start of week 1970.01.05
                arrayOf(listOf<Any>(1, hourInMs, DayOfWeek.MONDAY, 345600000L + hourInMs - 1), "Jan 5 - Jan 11"),
                arrayOf(listOf<Any>(1, hourInMs, DayOfWeek.MONDAY, 345600000L + hourInMs + 1), "Jan 12 - Jan 18"),

                // positive start of day, first day of week changed to start of week
                arrayOf(listOf<Any>(1, hourInMs, DayOfWeek.THURSDAY, hourInMs - 1), "Jan 1 - Jan 7"),
                arrayOf(listOf<Any>(1, hourInMs, DayOfWeek.THURSDAY, hourInMs + 1), "Jan 8 - Jan 14"),

                // positive start of day, end of week 1970.01.11
                arrayOf(listOf<Any>(1, hourInMs, DayOfWeek.MONDAY, 864000000L + 25 * hourInMs - 1), "Jan 12 - Jan 18"),
                arrayOf(listOf<Any>(1, hourInMs, DayOfWeek.MONDAY, 864000000L + 25 * hourInMs + 1), "Jan 19 - Jan 25"),

                // positive start of day, first day of week changed to end of week
                arrayOf(listOf<Any>(1, hourInMs, DayOfWeek.FRIDAY, 25 * hourInMs - 1), "Jan 2 - Jan 8"),
                arrayOf(listOf<Any>(1, hourInMs, DayOfWeek.FRIDAY, 25 * hourInMs + 1), "Jan 9 - Jan 15"),
            )
        }
    }

    @RunWith(Parameterized::class)
    class ToMonthTitleTest(
        private val input: List<Any>,
        private val output: String,
    ) {

        @Before
        fun before() {
            `when`(resourceRepo.getString(R.string.title_this_month)).thenReturn("this month")
        }

        @Test
        fun toMonthTitle() {
            `when`(currentTimestampProvider.get()).thenReturn(input[2] as Long)
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(localeProvider, resourceRepo, currentTimestampProvider)

            assertEquals(
                "Test failed for params $input",
                output,
                subject.toMonthTitle(
                    monthsFromToday = input[0] as Int,
                    startOfDayShift = input[1] as Long,
                ),
            )
        }

        companion object {
            private val localeDefault: Locale = Locale.getDefault()
            private val timezoneDefault: TimeZone = TimeZone.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                Locale.setDefault(Locale.US)
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                Locale.setDefault(localeDefault)
                TimeZone.setDefault(timezoneDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // months from today, start of day shift, current timestamp in ms

                // default
                arrayOf(listOf<Any>(0, 0L, 0L), "this month"),
                arrayOf(listOf<Any>(0, 0L, 24 * hourInMs - 1), "this month"),

                // months from today
                arrayOf(listOf<Any>(-3, 0L, 0L), "October"),
                arrayOf(listOf<Any>(-2, 0L, 0L), "November"),
                arrayOf(listOf<Any>(-1, 0L, 0L), "December"),
                arrayOf(listOf<Any>(1, 0L, 0L), "February"),
                arrayOf(listOf<Any>(2, 0L, 0L), "March"),
                arrayOf(listOf<Any>(3, 0L, 0L), "April"),

                // from 2022.01.18
                arrayOf(listOf<Any>(-50, 0L, 1642506258000L), "November"),
                arrayOf(listOf<Any>(50, 0L, 1642506258000L), "March"),

                // negative start of day, middle of month 1970.01.15
                arrayOf(listOf<Any>(1, -hourInMs, 1209600000L - hourInMs - 1), "February"),
                arrayOf(listOf<Any>(1, -hourInMs, 1209600000L - hourInMs + 1), "February"),

                // negative start of day, start of month 1970.01.01
                arrayOf(listOf<Any>(1, -hourInMs, -hourInMs - 1), "January"),
                arrayOf(listOf<Any>(1, -hourInMs, -hourInMs + 1), "February"),

                // negative start of day, end of month 1970.01.31
                arrayOf(listOf<Any>(1, -hourInMs, 2592000000 + 23 * hourInMs - 1), "February"),
                arrayOf(listOf<Any>(1, -hourInMs, 2592000000 + 23 * hourInMs + 1), "March"),

                // positive start of day, middle of month 1970.01.15
                arrayOf(listOf<Any>(1, hourInMs, 1209600000L + hourInMs - 1), "February"),
                arrayOf(listOf<Any>(1, hourInMs, 1209600000L + hourInMs + 1), "February"),

                // positive start of day, start of month 1970.01.01
                arrayOf(listOf<Any>(1, hourInMs, hourInMs - 1), "January"),
                arrayOf(listOf<Any>(1, hourInMs, hourInMs + 1), "February"),

                // positive start of day, end of month 1970.01.31
                arrayOf(listOf<Any>(1, hourInMs, 2592000000 + 25 * hourInMs - 1), "February"),
                arrayOf(listOf<Any>(1, hourInMs, 2592000000 + 25 * hourInMs + 1), "March"),
            )
        }
    }

    @RunWith(Parameterized::class)
    class ToYearTitleTest(
        private val input: List<Any>,
        private val output: String,
    ) {

        @Before
        fun before() {
            `when`(resourceRepo.getString(R.string.title_this_year)).thenReturn("this year")
        }

        @Test
        fun toYearTitle() {
            `when`(currentTimestampProvider.get()).thenReturn(input[2] as Long)
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(localeProvider, resourceRepo, currentTimestampProvider)

            assertEquals(
                "Test failed for params $input",
                output,
                subject.toYearTitle(
                    yearsFromToday = input[0] as Int,
                    startOfDayShift = input[1] as Long,
                ),
            )
        }

        companion object {
            private val localeDefault: Locale = Locale.getDefault()
            private val timezoneDefault: TimeZone = TimeZone.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                Locale.setDefault(Locale.US)
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                Locale.setDefault(localeDefault)
                TimeZone.setDefault(timezoneDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // years from today, start of day shift, current timestamp in ms

                // default
                arrayOf(listOf<Any>(0, 0L, 0L), "this year"),
                arrayOf(listOf<Any>(0, 0L, 24 * hourInMs - 1), "this year"),

                // years from today
                arrayOf(listOf<Any>(-3, 0L, 0L), "1967"),
                arrayOf(listOf<Any>(-2, 0L, 0L), "1968"),
                arrayOf(listOf<Any>(-1, 0L, 0L), "1969"),
                arrayOf(listOf<Any>(1, 0L, 0L), "1971"),
                arrayOf(listOf<Any>(2, 0L, 0L), "1972"),
                arrayOf(listOf<Any>(3, 0L, 0L), "1973"),

                // from 2022.01.18
                arrayOf(listOf<Any>(-50, 0L, 1642506258000L), "1972"),
                arrayOf(listOf<Any>(50, 0L, 1642506258000L), "2072"),

                // negative start of day, middle of year 1970.06.15
                arrayOf(listOf<Any>(1, -hourInMs, 14256000000 - hourInMs - 1), "1971"),
                arrayOf(listOf<Any>(1, -hourInMs, 14256000000 - hourInMs + 1), "1971"),

                // negative start of day, start of year 1970.01.01
                arrayOf(listOf<Any>(1, -hourInMs, -hourInMs - 1), "1970"),
                arrayOf(listOf<Any>(1, -hourInMs, -hourInMs + 1), "1971"),

                // negative start of day, end of year 1970.12.31
                arrayOf(listOf<Any>(1, -hourInMs, 31449600000 + 23 * hourInMs - 1), "1971"),
                arrayOf(listOf<Any>(1, -hourInMs, 31449600000 + 23 * hourInMs + 1), "1972"),

                // positive start of day, middle of year 1970.06.15
                arrayOf(listOf<Any>(1, hourInMs, 14256000000 + hourInMs - 1), "1971"),
                arrayOf(listOf<Any>(1, hourInMs, 14256000000 + hourInMs + 1), "1971"),

                // positive start of day, start of year 1970.01.01
                arrayOf(listOf<Any>(1, hourInMs, hourInMs - 1), "1970"),
                arrayOf(listOf<Any>(1, hourInMs, hourInMs + 1), "1971"),

                // positive start of day, end of year 1970.12.31
                arrayOf(listOf<Any>(1, hourInMs, 31449600000 + 25 * hourInMs - 1), "1971"),
                arrayOf(listOf<Any>(1, hourInMs, 31449600000 + 25 * hourInMs + 1), "1972"),
            )
        }
    }
}