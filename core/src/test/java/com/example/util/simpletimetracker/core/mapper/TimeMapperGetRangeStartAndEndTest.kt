package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.mapper.TimeMapperGetRangeStartAndEndTest.Subject.currentTimestampProvider
import com.example.util.simpletimetracker.core.mapper.TimeMapperGetRangeStartAndEndTest.Subject.hourInMs
import com.example.util.simpletimetracker.core.mapper.TimeMapperGetRangeStartAndEndTest.Subject.resourceRepo
import com.example.util.simpletimetracker.core.provider.CurrentTimestampProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@RunWith(Enclosed::class)
class TimeMapperGetRangeStartAndEndTest {

    private object Subject {
        val resourceRepo: ResourceRepo = Mockito.mock(ResourceRepo::class.java)
        val currentTimestampProvider: CurrentTimestampProvider = Mockito.mock(CurrentTimestampProvider::class.java)
        val hourInMs = TimeUnit.HOURS.toMillis(1)
    }

    @RunWith(Parameterized::class)
    class RangeDayTest(
        private val input: List<Any>,
        private val output: Pair<Long, Long>,
    ) {

        @Test
        fun test() {
            val subject = TimeMapper(resourceRepo, currentTimestampProvider)

            `when`(currentTimestampProvider.get()).thenReturn(input[4] as Long)

            assertEquals(
                "Test failed for params $input",
                output,
                subject.getRangeStartAndEnd(
                    rangeLength = input[0] as RangeLength,
                    shift = input[1] as Int,
                    firstDayOfWeek = input[2] as DayOfWeek,
                    startOfDayShift = input[3] as Long,
                )
            )
        }

        companion object {
            private val timezoneDefault: TimeZone = TimeZone.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                TimeZone.setDefault(timezoneDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // range length, shift, first day of week, start of day shift, current timestamp in ms

                arrayOf(listOf(RangeLength.Day, 0, DayOfWeek.MONDAY, 0L, 0L), 0L to 86400000L),
                arrayOf(listOf(RangeLength.Day, -1, DayOfWeek.MONDAY, 0L, 0L), -86400000L to 0L),
                arrayOf(listOf(RangeLength.Day, 1, DayOfWeek.MONDAY, 0L, 0L), 86400000L to 172800000L),
                arrayOf(listOf(RangeLength.Day, -50, DayOfWeek.MONDAY, 0L, 4320000000L), 0L to 86400000L),
                arrayOf(listOf(RangeLength.Day, 50, DayOfWeek.MONDAY, 0L, 0L), 4320000000L to 4406400000L),

                arrayOf(
                    listOf(RangeLength.Day, 1, DayOfWeek.MONDAY, -hourInMs, -hourInMs - 1),
                    -hourInMs to 86400000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Day, 1, DayOfWeek.MONDAY, -hourInMs, -hourInMs + 1),
                    86400000L - hourInMs to 172800000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Day, 1, DayOfWeek.MONDAY, -hourInMs, 0L),
                    86400000L - hourInMs to 172800000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Day, 1, DayOfWeek.MONDAY, -hourInMs, hourInMs),
                    86400000L - hourInMs to 172800000L - hourInMs
                ),

                arrayOf(
                    listOf(RangeLength.Day, 1, DayOfWeek.MONDAY, hourInMs, -hourInMs),
                    hourInMs to 86400000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Day, 1, DayOfWeek.MONDAY, hourInMs, 0L),
                    hourInMs to 86400000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Day, 1, DayOfWeek.MONDAY, hourInMs, hourInMs - 1),
                    hourInMs to 86400000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Day, 1, DayOfWeek.MONDAY, hourInMs, hourInMs + 1),
                    86400000L + hourInMs to 172800000L + hourInMs
                ),
            )
        }
    }

    @RunWith(Parameterized::class)
    class RangeWeekTest(
        private val input: List<Any>,
        private val output: Pair<Long, Long>,
    ) {

        @Test
        fun test() {
            val subject = TimeMapper(resourceRepo, currentTimestampProvider)

            `when`(currentTimestampProvider.get()).thenReturn(input[4] as Long)

            assertEquals(
                "Test failed for params $input",
                output,
                subject.getRangeStartAndEnd(
                    rangeLength = input[0] as RangeLength,
                    shift = input[1] as Int,
                    firstDayOfWeek = input[2] as DayOfWeek,
                    startOfDayShift = input[3] as Long,
                )
            )
        }

        companion object {
            private val timezoneDefault: TimeZone = TimeZone.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                TimeZone.setDefault(timezoneDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // range length, shift, first day of week, start of day shift, current timestamp in ms

                // from 1970.01.15
                arrayOf(listOf(RangeLength.Week, 0, DayOfWeek.MONDAY, 0L, 1209600000L), 950400000L to 1555200000L),
                arrayOf(listOf(RangeLength.Week, -1, DayOfWeek.MONDAY, 0L, 1209600000L), 345600000L to 950400000L),
                arrayOf(listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, 0L, 1209600000L), 1555200000L to 2160000000L),
                arrayOf(listOf(RangeLength.Week, -50, DayOfWeek.MONDAY, 0L, 30844800000L), 345600000L to 950400000L),
                arrayOf(listOf(RangeLength.Week, 50, DayOfWeek.MONDAY, 0L, 604800000L), 30585600000L to 31190400000L),

                // first day of week
                // from 1970.01.15
                arrayOf(listOf(RangeLength.Week, 0, DayOfWeek.MONDAY, 0L, 1209600000L), 950400000L to 1555200000L),
                arrayOf(listOf(RangeLength.Week, 0, DayOfWeek.TUESDAY, 0L, 1209600000L), 1036800000L to 1641600000L),
                arrayOf(listOf(RangeLength.Week, 0, DayOfWeek.WEDNESDAY, 0L, 1209600000L), 1123200000L to 1728000000L),
                arrayOf(listOf(RangeLength.Week, 0, DayOfWeek.THURSDAY, 0L, 1209600000L), 1209600000L to 1814400000L),
                arrayOf(listOf(RangeLength.Week, 0, DayOfWeek.FRIDAY, 0L, 1209600000L), 691200000L to 1296000000L),
                arrayOf(listOf(RangeLength.Week, 0, DayOfWeek.SATURDAY, 0L, 1209600000L), 777600000L to 1382400000L),
                arrayOf(listOf(RangeLength.Week, 0, DayOfWeek.SUNDAY, 0L, 1209600000L), 864000000L to 1468800000L),

                // from 1970.01.08
                arrayOf(listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, 0L, 604800000L), 950400000L to 1555200000L),
                arrayOf(listOf(RangeLength.Week, 1, DayOfWeek.TUESDAY, 0L, 604800000L), 1036800000L to 1641600000L),
                arrayOf(listOf(RangeLength.Week, 1, DayOfWeek.WEDNESDAY, 0L, 604800000L), 1123200000L to 1728000000L),
                arrayOf(listOf(RangeLength.Week, 1, DayOfWeek.THURSDAY, 0L, 604800000L), 1209600000L to 1814400000L),
                arrayOf(listOf(RangeLength.Week, 1, DayOfWeek.FRIDAY, 0L, 604800000L), 691200000L to 1296000000L),
                arrayOf(listOf(RangeLength.Week, 1, DayOfWeek.SATURDAY, 0L, 604800000L), 777600000L to 1382400000L),
                arrayOf(listOf(RangeLength.Week, 1, DayOfWeek.SUNDAY, 0L, 604800000L), 864000000L to 1468800000L),

                // from 1970.01.22
                arrayOf(listOf(RangeLength.Week, -1, DayOfWeek.MONDAY, 0L, 1814400000L), 950400000L to 1555200000L),
                arrayOf(listOf(RangeLength.Week, -1, DayOfWeek.TUESDAY, 0L, 1814400000L), 1036800000L to 1641600000L),
                arrayOf(listOf(RangeLength.Week, -1, DayOfWeek.WEDNESDAY, 0L, 1814400000L), 1123200000L to 1728000000L),
                arrayOf(listOf(RangeLength.Week, -1, DayOfWeek.THURSDAY, 0L, 1814400000L), 1209600000L to 1814400000L),
                arrayOf(listOf(RangeLength.Week, -1, DayOfWeek.FRIDAY, 0L, 1814400000L), 691200000L to 1296000000L),
                arrayOf(listOf(RangeLength.Week, -1, DayOfWeek.SATURDAY, 0L, 1814400000L), 777600000L to 1382400000L),
                arrayOf(listOf(RangeLength.Week, -1, DayOfWeek.SUNDAY, 0L, 1814400000L), 864000000L to 1468800000L),

                // start of day
                // from 1970.01.12 monday, negative shift
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, 0L, 345600000L),
                    950400000L to 1555200000L
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, -hourInMs, 345600000L - hourInMs - 1),
                    345600000L - hourInMs to 950400000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, -hourInMs, 345600000L - hourInMs + 1),
                    950400000L - hourInMs to 1555200000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, -hourInMs, 345600000L),
                    950400000L - hourInMs to 1555200000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, -hourInMs, 345600000L + 1),
                    950400000L - hourInMs to 1555200000L - hourInMs
                ),

                // from 1970.01.18 sunday, negative shift
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, 0L, 864000000L),
                    950400000L to 1555200000L
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, -hourInMs, 864000000L + 23 * hourInMs - 1),
                    950400000L - hourInMs to 1555200000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, -hourInMs, 864000000L + 23 * hourInMs + 1),
                    1555200000L - hourInMs to 2160000000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, -hourInMs, 864000000L + 24 * hourInMs),
                    1555200000L - hourInMs to 2160000000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, -hourInMs, 864000000L + 24 * hourInMs + 1),
                    1555200000L - hourInMs to 2160000000L - hourInMs
                ),

                // from 1970.01.12 monday, positive shift
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, 0L, 345600000L),
                    950400000L to 1555200000L
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, hourInMs, 345600000L - 1),
                    345600000L + hourInMs to 950400000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, hourInMs, 345600000L),
                    345600000L + hourInMs to 950400000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, hourInMs, 345600000L + hourInMs - 1),
                    345600000L + hourInMs to 950400000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, hourInMs, 345600000L + hourInMs + 1),
                    950400000L + hourInMs to 1555200000L + hourInMs
                ),

                // from 1970.01.18 sunday, positive shift
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, 0L, 864000000L),
                    950400000L to 1555200000L
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, hourInMs, 864000000L + 24 * hourInMs - 1),
                    950400000L + hourInMs to 1555200000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, hourInMs, 864000000L + 24 * hourInMs),
                    950400000L + hourInMs to 1555200000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, hourInMs, 864000000L + 25 * hourInMs - 1),
                    950400000L + hourInMs to 1555200000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Week, 1, DayOfWeek.MONDAY, hourInMs, 864000000L + 25 * hourInMs + 1),
                    1555200000 + hourInMs to 2160000000 + hourInMs
                ),
            )
        }
    }

    @RunWith(Parameterized::class)
    class RangeMonthTest(
        private val input: List<Any>,
        private val output: Pair<Long, Long>,
    ) {

        @Test
        fun test() {
            val subject = TimeMapper(resourceRepo, currentTimestampProvider)

            `when`(currentTimestampProvider.get()).thenReturn(input[4] as Long)

            assertEquals(
                "Test failed for params $input",
                output,
                subject.getRangeStartAndEnd(
                    rangeLength = input[0] as RangeLength,
                    shift = input[1] as Int,
                    firstDayOfWeek = input[2] as DayOfWeek,
                    startOfDayShift = input[3] as Long,
                )
            )
        }

        companion object {
            private val timezoneDefault: TimeZone = TimeZone.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                TimeZone.setDefault(timezoneDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // range length, shift, first day of week, start of day shift, current timestamp in ms

                arrayOf(listOf(RangeLength.Month, 0, DayOfWeek.MONDAY, 0L, 1209600000L), 0L to 2678400000L),
                arrayOf(listOf(RangeLength.Month, -1, DayOfWeek.MONDAY, 0L, 3888000000L), 0L to 2678400000L),
                arrayOf(listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, 0L, 0L), 2678400000L to 5097600000L),
                arrayOf(listOf(RangeLength.Month, -50, DayOfWeek.MONDAY, 0L, 132537600000L), 0L to 2678400000L),
                arrayOf(listOf(RangeLength.Month, 50, DayOfWeek.MONDAY, 0L, 0L), 131328000000L to 134006400000L),

                // from 1970.02.01 start of month, negative shift
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, 0L, 0L),
                    2678400000L to 5097600000L
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, -hourInMs, -hourInMs - 1),
                    0L - hourInMs to 2678400000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, -hourInMs, -hourInMs + 1),
                    2678400000L - hourInMs to 5097600000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, -hourInMs, 0L),
                    2678400000L - hourInMs to 5097600000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, -hourInMs, 1L),
                    2678400000L - hourInMs to 5097600000L - hourInMs
                ),

                // from 1970.02.28 end of month, negative shift
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, 0L, 2592000000L),
                    2678400000L to 5097600000L
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, -hourInMs, 2592000000L + 23 * hourInMs - 1),
                    2678400000L - hourInMs to 5097600000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, -hourInMs, 2592000000L + 23 * hourInMs + 1),
                    5097600000L - hourInMs to 7776000000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, -hourInMs, 2592000000L + 24 * hourInMs),
                    5097600000L - hourInMs to 7776000000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, -hourInMs, 2592000000L + 24 * hourInMs + 1),
                    5097600000L - hourInMs to 7776000000L - hourInMs
                ),

                // from 1970.02.01 start of month, positive shift
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, 0L, 0L),
                    2678400000L to 5097600000L
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, hourInMs, -1L),
                    0L + hourInMs to 2678400000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, hourInMs, 0L),
                    0L + hourInMs to 2678400000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, hourInMs, +hourInMs - 1),
                    0L + hourInMs to 2678400000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, hourInMs, +hourInMs + 1),
                    2678400000L + hourInMs to 5097600000L + hourInMs
                ),

                // from 1970.02.28 end of month, positive shift
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, 0L, 2592000000L),
                    2678400000L to 5097600000L
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, hourInMs, 2592000000L + 24 * hourInMs - 1),
                    2678400000L + hourInMs to 5097600000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, hourInMs, 2592000000L + 24 * hourInMs),
                    2678400000L + hourInMs to 5097600000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, hourInMs, 2592000000L + 25 * hourInMs - 1),
                    2678400000L + hourInMs to 5097600000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Month, 1, DayOfWeek.MONDAY, hourInMs, 2592000000L + 25 * hourInMs + 1),
                    5097600000L + hourInMs to 7776000000L + hourInMs
                ),
            )
        }
    }


    @RunWith(Parameterized::class)
    class RangeYearTest(
        private val input: List<Any>,
        private val output: Pair<Long, Long>,
    ) {

        @Test
        fun test() {
            val subject = TimeMapper(resourceRepo, currentTimestampProvider)

            `when`(currentTimestampProvider.get()).thenReturn(input[4] as Long)

            assertEquals(
                "Test failed for params $input",
                output,
                subject.getRangeStartAndEnd(
                    rangeLength = input[0] as RangeLength,
                    shift = input[1] as Int,
                    firstDayOfWeek = input[2] as DayOfWeek,
                    startOfDayShift = input[3] as Long,
                )
            )
        }

        companion object {
            private val timezoneDefault: TimeZone = TimeZone.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                TimeZone.setDefault(timezoneDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // range length, shift, first day of week, start of day shift, current timestamp in ms

                arrayOf(listOf(RangeLength.Year, 0, DayOfWeek.MONDAY, 0L, 14256000000L), 0L to 31536000000L),
                arrayOf(listOf(RangeLength.Year, -1, DayOfWeek.MONDAY, 0L, 62985600000L), 0L to 31536000000L),
                arrayOf(listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, 0L, 0L), 31536000000L to 63072000000L),
                arrayOf(listOf(RangeLength.Year, -50, DayOfWeek.MONDAY, 0L, 1592179200000L), 0L to 31536000000L),
                arrayOf(listOf(RangeLength.Year, 50, DayOfWeek.MONDAY, 0L, 0L), 1577836800000L to 1609459200000L),

                // from 1971.01.01 start of year, negative shift
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, 0L, 0L),
                    31536000000L to 63072000000L
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, -hourInMs, -hourInMs - 1),
                    0L - hourInMs to 31536000000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, -hourInMs, -hourInMs + 1),
                    31536000000L - hourInMs to 63072000000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, -hourInMs, 0L),
                    31536000000L - hourInMs to 63072000000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, -hourInMs, 1L),
                    31536000000L - hourInMs to 63072000000L - hourInMs
                ),

                // from 1971.12.31 end of year, negative shift
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, 0L, 31449600000L),
                    31536000000L to 63072000000L
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, -hourInMs, 31449600000L + 23 * hourInMs - 1),
                    31536000000L - hourInMs to 63072000000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, -hourInMs, 31449600000L + 23 * hourInMs + 1),
                    63072000000L - hourInMs to 94694400000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, -hourInMs, 31449600000L + 24 * hourInMs),
                    63072000000L - hourInMs to 94694400000L - hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, -hourInMs, 31449600000L + 24 * hourInMs + 1),
                    63072000000L - hourInMs to 94694400000L - hourInMs
                ),

                // from 1971.01.01 start of year, positive shift
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, 0L, 0L),
                    31536000000L to 63072000000L
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, hourInMs, -1L),
                    0L + hourInMs to 31536000000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, hourInMs, 0L),
                    0L + hourInMs to 31536000000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, hourInMs, +hourInMs - 1),
                    0L + hourInMs to 31536000000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, hourInMs, +hourInMs + 1),
                    31536000000L + hourInMs to 63072000000L + hourInMs
                ),

                // from 1971.12.31 end of year, positive shift
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, 0L, 31449600000L),
                    31536000000L to 63072000000L
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, hourInMs, 31449600000L + 24 * hourInMs - 1),
                    31536000000L + hourInMs to 63072000000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, hourInMs, 31449600000L + 24 * hourInMs),
                    31536000000L + hourInMs to 63072000000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, hourInMs, 31449600000L + 25 * hourInMs - 1),
                    31536000000L + hourInMs to 63072000000L + hourInMs
                ),
                arrayOf(
                    listOf(RangeLength.Year, 1, DayOfWeek.MONDAY, hourInMs, 31449600000L + 25 * hourInMs + 1),
                    63072000000L + hourInMs to 94694400000L + hourInMs
                ),
            )
        }
    }

    @RunWith(Parameterized::class)
    class OtherRangesTest(
        private val input: List<Any>,
        private val output: Pair<Long, Long>,
    ) {

        @Test
        fun test() {
            val subject = TimeMapper(resourceRepo, currentTimestampProvider)

            `when`(currentTimestampProvider.get()).thenReturn(input[4] as Long)

            assertEquals(
                "Test failed for params $input",
                output,
                subject.getRangeStartAndEnd(
                    rangeLength = input[0] as RangeLength,
                    shift = input[1] as Int,
                    firstDayOfWeek = input[2] as DayOfWeek,
                    startOfDayShift = input[3] as Long,
                )
            )
        }

        companion object {
            private val timezoneDefault: TimeZone = TimeZone.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                TimeZone.setDefault(timezoneDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // range length, shift, first day of week, start of day shift, current timestamp in ms

                // range all
                arrayOf(listOf(RangeLength.All, 0, DayOfWeek.MONDAY, 0L, 0L), 0L to 0L),

                arrayOf(listOf(RangeLength.All, -1, DayOfWeek.MONDAY, 0L, 0L), 0L to 0L),
                arrayOf(listOf(RangeLength.All, 1, DayOfWeek.MONDAY, 0L, 0L), 0L to 0L),

                arrayOf(listOf(RangeLength.All, 0, DayOfWeek.WEDNESDAY, 0L, 0L), 0L to 0L),
                arrayOf(listOf(RangeLength.All, 0, DayOfWeek.MONDAY, hourInMs, 0L), 0L to 0L),
                arrayOf(listOf(RangeLength.All, 0, DayOfWeek.MONDAY, 0L, hourInMs), 0L to 0L),

                // range custom
                arrayOf(
                    listOf(RangeLength.Custom(Range(100L, 200L)), 0, DayOfWeek.MONDAY, 0L, 0L),
                    100L to 200L
                ),
                arrayOf(
                    listOf(RangeLength.Custom(Range(300L, 400L)), 0, DayOfWeek.WEDNESDAY, hourInMs, hourInMs),
                    300L to 400L
                ),
            )
        }
    }
}