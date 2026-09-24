package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.currentTimestampProvider
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.dayInMs
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.hourInMs
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.initStrings
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.localeProvider
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.minuteInMs
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.resourceRepo
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.secondInMs
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import org.junit.After
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
class TimeMapperTest {

    private object Subject {
        val resourceRepo: ResourceRepo = Mockito.mock(ResourceRepo::class.java)
        val currentTimestampProvider: CurrentTimestampProvider = Mockito.mock(CurrentTimestampProvider::class.java)
        val localeProvider: LocaleProvider = Mockito.mock(LocaleProvider::class.java)

        val secondInMs = TimeUnit.SECONDS.toMillis(1)
        val minuteInMs = TimeUnit.MINUTES.toMillis(1)
        val hourInMs = TimeUnit.HOURS.toMillis(1)
        val dayInMs = TimeUnit.DAYS.toMillis(1)

        fun initStrings() {
            `when`(resourceRepo.getString(R.string.time_day)).thenReturn("d")
            `when`(resourceRepo.getString(R.string.time_hour)).thenReturn("h")
            `when`(resourceRepo.getString(R.string.time_minute)).thenReturn("m")
            `when`(resourceRepo.getString(R.string.time_second)).thenReturn("s")
        }
    }

    class FormatTimeTimeZoneChangeTest {

        private lateinit var timeZoneDefault: TimeZone

        @Before
        fun before() {
            timeZoneDefault = TimeZone.getDefault()
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            `when`(localeProvider.get()).thenReturn(Locale.US)
        }

        @After
        fun after() {
            TimeZone.setDefault(timeZoneDefault)
        }

        @Test
        fun formatTimeAfterTimeZoneChanged() {
            val subject = TimeMapper(
                localeProvider = localeProvider,
                resourceRepo = resourceRepo,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "12:00 AM",
                subject.formatTime(time = 0, useMilitaryTime = false, showSeconds = false),
            )

            TimeZone.setDefault(TimeZone.getTimeZone("GMT+03:00"))

            assertEquals(
                "3:00 AM",
                subject.formatTime(time = 0, useMilitaryTime = false, showSeconds = false),
            )
        }
    }

    @RunWith(Parameterized::class)
    class FormatIntervalTest(
        private val input: Long,
        private val output: String,
    ) {

        @Before
        fun before() {
            initStrings()
        }

        @Test
        fun formatInterval() {
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(
                localeProvider = localeProvider,
                resourceRepo = resourceRepo,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.formatInterval(
                    input,
                    forceSeconds = false,
                    durationFormat = DurationFormat.HOURS,
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                arrayOf(0, "0m"),
                arrayOf(100, "0m"),
                arrayOf(secondInMs, "0m"),
                arrayOf(14 * secondInMs, "0m"),

                arrayOf(minuteInMs, "1m"),
                arrayOf(minuteInMs + 100, "1m"),
                arrayOf(minuteInMs + secondInMs, "1m"),
                arrayOf(2 * minuteInMs, "2m"),
                arrayOf(12 * minuteInMs + 34 * secondInMs, "12m"),
                arrayOf(59 * minuteInMs, "59m"),

                arrayOf(hourInMs, "1h 0m"),
                arrayOf(hourInMs + minuteInMs, "1h 1m"),
                arrayOf(hourInMs + minuteInMs + secondInMs, "1h 1m"),
                arrayOf(12 * hourInMs + 34 * minuteInMs + 56 * secondInMs, "12h 34m"),

                arrayOf(dayInMs, "24h 0m"),
                arrayOf(dayInMs + hourInMs, "25h 0m"),
                arrayOf(dayInMs + hourInMs + minuteInMs, "25h 1m"),
                arrayOf(dayInMs + hourInMs + minuteInMs + secondInMs, "25h 1m"),
                arrayOf(12 * dayInMs + 34 * hourInMs + 56 * minuteInMs + 78 * secondInMs, "322h 57m"),

                arrayOf(-hourInMs, "-1h 0m"),
            )
        }
    }

    @RunWith(Parameterized::class)
    class FormatIntervalDaysTest(
        private val input: Long,
        private val output: String,
    ) {

        @Before
        fun before() {
            initStrings()
        }

        @Test
        fun formatInterval() {
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(
                localeProvider = localeProvider,
                resourceRepo = resourceRepo,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.formatInterval(
                    input,
                    forceSeconds = false,
                    durationFormat = DurationFormat.DAYS,
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                arrayOf(0, "0m"),
                arrayOf(100, "0m"),
                arrayOf(secondInMs, "0m"),
                arrayOf(14 * secondInMs, "0m"),

                arrayOf(minuteInMs, "1m"),
                arrayOf(minuteInMs + 100, "1m"),
                arrayOf(minuteInMs + secondInMs, "1m"),
                arrayOf(2 * minuteInMs, "2m"),
                arrayOf(12 * minuteInMs + 34 * secondInMs, "12m"),
                arrayOf(59 * minuteInMs, "59m"),

                arrayOf(hourInMs, "1h 0m"),
                arrayOf(hourInMs + minuteInMs, "1h 1m"),
                arrayOf(hourInMs + minuteInMs + secondInMs, "1h 1m"),
                arrayOf(12 * hourInMs + 34 * minuteInMs + 56 * secondInMs, "12h 34m"),

                arrayOf(dayInMs, "1d 0h 0m"),
                arrayOf(dayInMs + hourInMs, "1d 1h 0m"),
                arrayOf(dayInMs + hourInMs + minuteInMs, "1d 1h 1m"),
                arrayOf(dayInMs + hourInMs + minuteInMs + secondInMs, "1d 1h 1m"),
                arrayOf(12 * dayInMs + 34 * hourInMs + 56 * minuteInMs + 78 * secondInMs, "13d 10h 57m"),

                arrayOf(-hourInMs, "-1h 0m"),
            )
        }
    }

    @RunWith(Parameterized::class)
    class FormatIntervalMinutesTest(
        private val input: Long,
        private val output: String,
    ) {

        @Before
        fun before() {
            initStrings()
        }

        @Test
        fun formatInterval() {
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(
                localeProvider = localeProvider,
                resourceRepo = resourceRepo,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.formatInterval(
                    input,
                    forceSeconds = false,
                    durationFormat = DurationFormat.MINUTES,
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                arrayOf(0, "0m"),
                arrayOf(100, "0m"),
                arrayOf(secondInMs, "0m"),
                arrayOf(14 * secondInMs, "0m"),

                arrayOf(minuteInMs, "1m"),
                arrayOf(minuteInMs + 100, "1m"),
                arrayOf(minuteInMs + secondInMs, "1m"),
                arrayOf(2 * minuteInMs, "2m"),
                arrayOf(12 * minuteInMs + 34 * secondInMs, "12m"),
                arrayOf(59 * minuteInMs, "59m"),

                arrayOf(hourInMs, "60m"),
                arrayOf(hourInMs + minuteInMs, "61m"),
                arrayOf(hourInMs + minuteInMs + secondInMs, "61m"),
                arrayOf(12 * hourInMs + 34 * minuteInMs + 56 * secondInMs, "754m"),

                arrayOf(dayInMs, "1440m"),
                arrayOf(dayInMs + hourInMs, "1500m"),
                arrayOf(dayInMs + hourInMs + minuteInMs, "1501m"),
                arrayOf(dayInMs + hourInMs + minuteInMs + secondInMs, "1501m"),
                arrayOf(12 * dayInMs + 34 * hourInMs + 56 * minuteInMs + 78 * secondInMs, "19377m"),

                arrayOf(-hourInMs, "-60m"),
            )
        }
    }

    @RunWith(Parameterized::class)
    class FormatIntervalProportionalTest(
        private val input: Long,
        private val output: String,
    ) {

        @Before
        fun before() {
            initStrings()
        }

        @Test
        fun formatInterval() {
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(
                localeProvider = localeProvider,
                resourceRepo = resourceRepo,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.formatInterval(
                    input,
                    forceSeconds = false,
                    durationFormat = DurationFormat.PROPORTIONAL_MINUTES,
                ),
            )
        }

        companion object {
            private val localeDefault: Locale = Locale.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                Locale.setDefault(Locale.US)
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                Locale.setDefault(localeDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                arrayOf(0, "0.00h"),
                arrayOf(100, "0.00h"),
                arrayOf(secondInMs, "0.00h"),
                arrayOf(14 * secondInMs, "0.00h"),

                arrayOf(minuteInMs, "0.02h"),
                arrayOf(minuteInMs + 100, "0.02h"),
                arrayOf(minuteInMs + secondInMs, "0.02h"),
                arrayOf(2 * minuteInMs, "0.03h"),
                arrayOf(12 * minuteInMs + 34 * secondInMs, "0.20h"),
                arrayOf(59 * minuteInMs, "0.98h"),

                arrayOf(hourInMs, "1.00h"),
                arrayOf(hourInMs + minuteInMs, "1.02h"),
                arrayOf(hourInMs + minuteInMs + secondInMs, "1.02h"),
                arrayOf(12 * hourInMs + 34 * minuteInMs + 56 * secondInMs, "12.57h"),

                arrayOf(dayInMs, "24.00h"),
                arrayOf(dayInMs + hourInMs, "25.00h"),
                arrayOf(dayInMs + hourInMs + minuteInMs, "25.02h"),
                arrayOf(dayInMs + hourInMs + minuteInMs + secondInMs, "25.02h"),
                arrayOf(12 * dayInMs + 34 * hourInMs + 56 * minuteInMs + 78 * secondInMs, "322.95h"),

                arrayOf(-hourInMs, "-1.00h"),
            )
        }
    }

    @RunWith(Parameterized::class)
    class FormatIntervalWithForcedSecondsTest(
        private val input: Long,
        private val output: String,
    ) {

        @Before
        fun before() {
            initStrings()
        }

        @Test
        fun formatInterval() {
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(
                localeProvider = localeProvider,
                resourceRepo = resourceRepo,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.formatInterval(
                    input,
                    forceSeconds = true,
                    durationFormat = DurationFormat.HOURS,
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                arrayOf(0, "0s"),
                arrayOf(100, "0s"),
                arrayOf(secondInMs, "1s"),
                arrayOf(14 * secondInMs, "14s"),

                arrayOf(minuteInMs, "1m 0s"),
                arrayOf(minuteInMs + 100, "1m 0s"),
                arrayOf(minuteInMs + secondInMs, "1m 1s"),
                arrayOf(2 * minuteInMs, "2m 0s"),
                arrayOf(12 * minuteInMs + 34 * secondInMs, "12m 34s"),
                arrayOf(59 * minuteInMs, "59m 0s"),

                arrayOf(hourInMs, "1h 0m 0s"),
                arrayOf(hourInMs + minuteInMs, "1h 1m 0s"),
                arrayOf(hourInMs + minuteInMs + secondInMs, "1h 1m 1s"),
                arrayOf(12 * hourInMs + 34 * minuteInMs + 56 * secondInMs, "12h 34m 56s"),

                arrayOf(dayInMs, "24h 0m 0s"),
                arrayOf(dayInMs + hourInMs, "25h 0m 0s"),
                arrayOf(dayInMs + hourInMs + minuteInMs, "25h 1m 0s"),
                arrayOf(dayInMs + hourInMs + minuteInMs + secondInMs, "25h 1m 1s"),
                arrayOf(12 * dayInMs + 34 * hourInMs + 56 * minuteInMs + 78 * secondInMs, "322h 57m 18s"),

                arrayOf(-hourInMs, "-1h 0m 0s"),
            )
        }
    }
}