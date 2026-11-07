package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapper.CalendarRange
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapperTest.Subject.currentTimestampProvider
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapperTest.Subject.daysInCalendarMapper
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapperTest.Subject.monday
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapperTest.Subject.sunday
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapperTest.Subject.timeMapper
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapperTest.Subject.tuesday
import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.provider.MutableCalendarTypeProvider
import com.example.util.simpletimetracker.core.provider.TimeZoneProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.daysOfWeek.mapper.DaysInCalendarMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.extension.orZero
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito
import org.mockito.Mockito.`when`

@RunWith(Enclosed::class)
class CalendarToListShiftMapperTest {

    private object Subject {
        var monday = 1743379200000L
        var tuesday = 1743465600000L
        var sunday = 1743897600000L

        val resourceRepo: ResourceRepo = Mockito.mock(ResourceRepo::class.java)
        val localeProvider: LocaleProvider = Mockito.mock(LocaleProvider::class.java)
        val currentTimestampProvider: CurrentTimestampProvider = Mockito.mock(CurrentTimestampProvider::class.java)
        val timeMapper: TimeMapper = TimeMapper(localeProvider, resourceRepo, currentTimestampProvider, MutableCalendarTypeProvider(), TimeZoneProvider())
        val daysInCalendarMapper: DaysInCalendarMapper = DaysInCalendarMapper()
    }

    @RunWith(Parameterized::class)
    class MapCalendarToListShift(
        private val input: List<Any>,
        private val output: CalendarRange,
    ) {

        @Test
        fun test() {
            val timestamp = input.getOrNull(4) as? Long
            `when`(currentTimestampProvider.get()).thenReturn(timestamp.orZero())

            val subject = CalendarToListShiftMapper(
                timeMapper = timeMapper,
                daysInCalendarMapper = daysInCalendarMapper,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.mapCalendarToListShift(
                    calendarShift = input[0] as Int,
                    daysInCalendar = input[1] as DaysInCalendar,
                    startOfDayShift = input[2] as Long,
                    firstDayOfWeek = input[3] as DayOfWeek,
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // calendarShift, calendarDayCount

                // Days 1
                arrayOf(listOf(-13, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), CalendarRange(-13, -13)),
                arrayOf(listOf(-2, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), CalendarRange(-2, -2)),
                arrayOf(listOf(-1, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), CalendarRange(-1, -1)),
                arrayOf(listOf(0, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), CalendarRange(0, 0)),
                arrayOf(listOf(1, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), CalendarRange(1, 1)),
                arrayOf(listOf(2, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), CalendarRange(2, 2)),
                arrayOf(listOf(13, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), CalendarRange(13, 13)),

                // Days 3
                arrayOf(listOf(-13, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), CalendarRange(-41, -39)),
                arrayOf(listOf(-2, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), CalendarRange(-8, -6)),
                arrayOf(listOf(-1, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), CalendarRange(-5, -3)),
                arrayOf(listOf(0, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), CalendarRange(-2, 0)),
                arrayOf(listOf(1, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), CalendarRange(1, 3)),
                arrayOf(listOf(2, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), CalendarRange(4, 6)),
                arrayOf(listOf(13, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), CalendarRange(37, 39)),

                // Days 5
                arrayOf(listOf(-13, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), CalendarRange(-69, -65)),
                arrayOf(listOf(-2, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), CalendarRange(-14, -10)),
                arrayOf(listOf(-1, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), CalendarRange(-9, -5)),
                arrayOf(listOf(0, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), CalendarRange(-4, 0)),
                arrayOf(listOf(1, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), CalendarRange(1, 5)),
                arrayOf(listOf(2, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), CalendarRange(6, 10)),
                arrayOf(listOf(13, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), CalendarRange(61, 65)),

                // Days 7
                arrayOf(listOf(-13, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), CalendarRange(-97, -91)),
                arrayOf(listOf(-2, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), CalendarRange(-20, -14)),
                arrayOf(listOf(-1, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), CalendarRange(-13, -7)),
                arrayOf(listOf(0, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), CalendarRange(-6, 0)),
                arrayOf(listOf(1, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), CalendarRange(1, 7)),
                arrayOf(listOf(2, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), CalendarRange(8, 14)),
                arrayOf(listOf(13, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), CalendarRange(85, 91)),

                // Week
                arrayOf(listOf(-1, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), CalendarRange(-7, -1)),
                arrayOf(listOf(0, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), CalendarRange(0, 6)),
                arrayOf(listOf(1, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), CalendarRange(7, 13)),
                arrayOf(listOf(-1, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), CalendarRange(-8, -2)),
                arrayOf(listOf(0, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), CalendarRange(-1, 5)),
                arrayOf(listOf(1, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), CalendarRange(6, 12)),
                arrayOf(listOf(-1, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), CalendarRange(-13, -7)),
                arrayOf(listOf(0, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), CalendarRange(-6, 0)),
                arrayOf(listOf(1, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), CalendarRange(1, 7)),

                arrayOf(listOf(-1, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), CalendarRange(-8, -2)),
                arrayOf(listOf(0, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), CalendarRange(-1, 5)),
                arrayOf(listOf(1, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), CalendarRange(6, 12)),
                arrayOf(listOf(-1, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), CalendarRange(-9, -3)),
                arrayOf(listOf(0, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), CalendarRange(-2, 4)),
                arrayOf(listOf(1, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), CalendarRange(5, 11)),
                arrayOf(listOf(-1, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), CalendarRange(-7, -1)),
                arrayOf(listOf(0, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), CalendarRange(0, 6)),
                arrayOf(listOf(1, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), CalendarRange(7, 13)),
            )
        }
    }

    @RunWith(Parameterized::class)
    class MapListToCalendarShift(
        private val input: List<Any>,
        private val output: Int,
    ) {

        @Test
        fun test() {
            val timestamp = input.getOrNull(4) as? Long
            `when`(currentTimestampProvider.get()).thenReturn(timestamp.orZero())

            val subject = CalendarToListShiftMapper(
                timeMapper = timeMapper,
                daysInCalendarMapper = daysInCalendarMapper,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.mapListToCalendarShift(
                    listShift = input[0] as Int,
                    daysInCalendar = input[1] as DaysInCalendar,
                    startOfDayShift = input[2] as Long,
                    firstDayOfWeek = input[3] as DayOfWeek,
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // listShift, calendarDayCount

                // Days 1
                arrayOf(listOf(-13, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), -13),
                arrayOf(listOf(-2, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), -2),
                arrayOf(listOf(-1, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), -1),
                arrayOf(listOf(0, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(1, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(2, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), 2),
                arrayOf(listOf(13, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), 13),

                // Days 3
                arrayOf(listOf(-13, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -4),
                arrayOf(listOf(-6, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -2),
                arrayOf(listOf(-5, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -1),
                arrayOf(listOf(-4, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -1),
                arrayOf(listOf(-3, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -1),
                arrayOf(listOf(-2, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(-1, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(0, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(1, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(2, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(3, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(4, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 2),
                arrayOf(listOf(13, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 5),

                // Days 5
                arrayOf(listOf(-10, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), -2),
                arrayOf(listOf(-9, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), -1),
                arrayOf(listOf(-5, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), -1),
                arrayOf(listOf(-4, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(-3, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(-2, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(-1, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(0, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(1, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(2, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(3, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(4, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(5, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(6, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 2),
                arrayOf(listOf(10, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 2),
                arrayOf(listOf(15, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 3),

                // Days 7
                arrayOf(listOf(-14, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), -2),
                arrayOf(listOf(-13, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), -1),
                arrayOf(listOf(-7, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), -1),
                arrayOf(listOf(-6, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(0, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(1, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(7, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(8, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 2),
                arrayOf(listOf(14, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 2),
                arrayOf(listOf(15, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 3),

                // Week
                arrayOf(listOf(-8, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), -2),
                arrayOf(listOf(-7, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), -1),
                arrayOf(listOf(-1, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), -1),
                arrayOf(listOf(0, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), 0),
                arrayOf(listOf(6, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), 0),
                arrayOf(listOf(7, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), 1),
                arrayOf(listOf(13, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), 1),
                arrayOf(listOf(14, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, monday), 2),

                arrayOf(listOf(-9, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), -2),
                arrayOf(listOf(-8, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), -1),
                arrayOf(listOf(-2, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), -1),
                arrayOf(listOf(-1, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), 0),
                arrayOf(listOf(5, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), 0),
                arrayOf(listOf(6, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), 1),
                arrayOf(listOf(12, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), 1),
                arrayOf(listOf(13, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, tuesday), 2),

                arrayOf(listOf(-14, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), -2),
                arrayOf(listOf(-13, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), -1),
                arrayOf(listOf(-7, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), -1),
                arrayOf(listOf(-6, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), 0),
                arrayOf(listOf(0, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), 0),
                arrayOf(listOf(1, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), 1),
                arrayOf(listOf(7, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), 1),
                arrayOf(listOf(8, DaysInCalendar.WEEK, 0L, DayOfWeek.MONDAY, sunday), 2),

                arrayOf(listOf(-9, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), -2),
                arrayOf(listOf(-8, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), -1),
                arrayOf(listOf(-2, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), -1),
                arrayOf(listOf(-1, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), 0),
                arrayOf(listOf(5, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), 0),
                arrayOf(listOf(6, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), 1),
                arrayOf(listOf(12, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), 1),
                arrayOf(listOf(13, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, monday), 2),

                arrayOf(listOf(-10, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), -2),
                arrayOf(listOf(-9, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), -1),
                arrayOf(listOf(-3, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), -1),
                arrayOf(listOf(-2, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), 0),
                arrayOf(listOf(4, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), 0),
                arrayOf(listOf(5, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), 1),
                arrayOf(listOf(11, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), 1),
                arrayOf(listOf(12, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, tuesday), 2),

                arrayOf(listOf(-8, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), -2),
                arrayOf(listOf(-7, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), -1),
                arrayOf(listOf(-1, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), -1),
                arrayOf(listOf(0, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), 0),
                arrayOf(listOf(6, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), 0),
                arrayOf(listOf(7, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), 1),
                arrayOf(listOf(13, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), 1),
                arrayOf(listOf(14, DaysInCalendar.WEEK, 0L, DayOfWeek.SUNDAY, sunday), 2),
            )
        }
    }

    @RunWith(Parameterized::class)
    class RecalculateRangeOnCalendarViewSwitched(
        private val input: List<Any>,
        private val output: Int,
    ) {

        @Test
        fun test() {
            val subject = CalendarToListShiftMapper(
                timeMapper = timeMapper,
                daysInCalendarMapper = daysInCalendarMapper,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.recalculateRangeOnCalendarViewSwitched(
                    currentPosition = input[0] as Int,
                    lastListPosition = input[1] as Int,
                    showCalendar = input[2] as Boolean,
                    daysInCalendar = input[3] as DaysInCalendar,
                    startOfDayShift = input[4] as Long,
                    firstDayOfWeek = input[5] as DayOfWeek,
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // currentPosition, lastListPosition, showCalendar, daysInCalendar, startOfDayShift, firstDayOfWeek

                // List to calendar
                arrayOf(listOf(-13, 0, true, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -4),
                arrayOf(listOf(0, 0, true, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(13, 0, true, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 5),
                arrayOf(listOf(-15, 0, true, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), -3),
                arrayOf(listOf(0, 0, true, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(11, 0, true, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 3),

                // Calendar to list, can't restore last saved position
                arrayOf(listOf(-13, 999, false, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -39),
                arrayOf(listOf(0, 999, false, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(13, 999, false, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 39),
                arrayOf(listOf(-13, 999, false, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), -65),
                arrayOf(listOf(0, 999, false, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(13, 999, false, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 65),

                // Calendar to list, can't restore last saved position, can restore to today
                arrayOf(listOf(0, -3, false, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 0),

                // Calendar to list, can restore last saved position
                arrayOf(listOf(-13, -41, false, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -41),
                arrayOf(listOf(0, -1, false, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -1),
                arrayOf(listOf(13, 37, false, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 37),
                arrayOf(listOf(-13, -68, false, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), -68),
                arrayOf(listOf(0, -3, false, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), -3),
                arrayOf(listOf(13, 64, false, DaysInCalendar.FIVE, 0L, DayOfWeek.MONDAY), 64),
            )
        }
    }

    @RunWith(Parameterized::class)
    class RecalculateRangeOnCalendarDaysChanged(
        private val input: List<Any>,
        private val output: Int,
    ) {

        @Test
        fun test() {
            val subject = CalendarToListShiftMapper(
                timeMapper = timeMapper,
                daysInCalendarMapper = daysInCalendarMapper,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.recalculateRangeOnCalendarDaysChanged(
                    currentPosition = input[0] as Int,
                    currentDaysInCalendar = input[1] as DaysInCalendar,
                    newDaysInCalendar = input[2] as DaysInCalendar,
                    startOfDayShift = input[3] as Long,
                    firstDayOfWeek = input[4] as DayOfWeek,
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // currentPosition, currentDaysInCalendar, newDaysInCalendar, startOfDayShift, firstDayOfWeek

                // From 1 to 3
                arrayOf(listOf(-13, DaysInCalendar.ONE, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -4),
                arrayOf(listOf(-3, DaysInCalendar.ONE, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -1),
                arrayOf(listOf(-2, DaysInCalendar.ONE, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(-1, DaysInCalendar.ONE, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(0, DaysInCalendar.ONE, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(1, DaysInCalendar.ONE, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(2, DaysInCalendar.ONE, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(3, DaysInCalendar.ONE, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(4, DaysInCalendar.ONE, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 2),
                arrayOf(listOf(13, DaysInCalendar.ONE, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 5),

                // From 3 to 1
                arrayOf(listOf(-13, DaysInCalendar.THREE, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), -39),
                arrayOf(listOf(-2, DaysInCalendar.THREE, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), -6),
                arrayOf(listOf(-1, DaysInCalendar.THREE, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), -3),
                arrayOf(listOf(0, DaysInCalendar.THREE, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(1, DaysInCalendar.THREE, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), 3),
                arrayOf(listOf(2, DaysInCalendar.THREE, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), 6),
                arrayOf(listOf(13, DaysInCalendar.THREE, DaysInCalendar.ONE, 0L, DayOfWeek.MONDAY), 39),

                // From 3 to 7
                arrayOf(listOf(-13, DaysInCalendar.THREE, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), -5),
                arrayOf(listOf(-2, DaysInCalendar.THREE, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(-1, DaysInCalendar.THREE, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(0, DaysInCalendar.THREE, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(1, DaysInCalendar.THREE, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(2, DaysInCalendar.THREE, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 1),
                arrayOf(listOf(13, DaysInCalendar.THREE, DaysInCalendar.SEVEN, 0L, DayOfWeek.MONDAY), 6),

                // From 7 to 3
                arrayOf(listOf(-13, DaysInCalendar.SEVEN, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -30),
                arrayOf(listOf(-2, DaysInCalendar.SEVEN, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -4),
                arrayOf(listOf(-1, DaysInCalendar.SEVEN, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), -2),
                arrayOf(listOf(0, DaysInCalendar.SEVEN, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 0),
                arrayOf(listOf(1, DaysInCalendar.SEVEN, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 3),
                arrayOf(listOf(2, DaysInCalendar.SEVEN, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 5),
                arrayOf(listOf(13, DaysInCalendar.SEVEN, DaysInCalendar.THREE, 0L, DayOfWeek.MONDAY), 31),
            )
        }
    }
}