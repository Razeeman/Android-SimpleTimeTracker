package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapper.CalendarRange
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Enclosed::class)
class CalendarToListShiftMapperTest {

    @RunWith(Parameterized::class)
    class MapCalendarToListShift(
        private val input: List<Int>,
        private val output: CalendarRange,
    ) {

        @Test
        fun test() {
            val subject = CalendarToListShiftMapper()

            assertEquals(
                "Test failed for params $input",
                output,
                subject.mapCalendarToListShift(
                    calendarShift = input[0],
                    calendarDayCount = input[1],
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // calendarShift, calendarDayCount

                // Days 0, should be impossible, check just in case.
                arrayOf(listOf(0, 0), CalendarRange(0, 0)),

                // Days 1
                arrayOf(listOf(-13, 1), CalendarRange(-13, -13)),
                arrayOf(listOf(-2, 1), CalendarRange(-2, -2)),
                arrayOf(listOf(-1, 1), CalendarRange(-1, -1)),
                arrayOf(listOf(0, 1), CalendarRange(0, 0)),
                arrayOf(listOf(1, 1), CalendarRange(1, 1)),
                arrayOf(listOf(2, 1), CalendarRange(2, 2)),
                arrayOf(listOf(13, 1), CalendarRange(13, 13)),

                // Days 3
                arrayOf(listOf(-13, 3), CalendarRange(-41, -39)),
                arrayOf(listOf(-2, 3), CalendarRange(-8, -6)),
                arrayOf(listOf(-1, 3), CalendarRange(-5, -3)),
                arrayOf(listOf(0, 3), CalendarRange(-2, 0)),
                arrayOf(listOf(1, 3), CalendarRange(1, 3)),
                arrayOf(listOf(2, 3), CalendarRange(4, 6)),
                arrayOf(listOf(13, 3), CalendarRange(37, 39)),

                // Days 5
                arrayOf(listOf(-13, 5), CalendarRange(-69, -65)),
                arrayOf(listOf(-2, 5), CalendarRange(-14, -10)),
                arrayOf(listOf(-1, 5), CalendarRange(-9, -5)),
                arrayOf(listOf(0, 5), CalendarRange(-4, 0)),
                arrayOf(listOf(1, 5), CalendarRange(1, 5)),
                arrayOf(listOf(2, 5), CalendarRange(6, 10)),
                arrayOf(listOf(13, 5), CalendarRange(61, 65)),
            )
        }
    }

    @RunWith(Parameterized::class)
    class MapListToCalendarShift(
        private val input: List<Int>,
        private val output: Int,
    ) {

        @Test
        fun test() {
            val subject = CalendarToListShiftMapper()

            assertEquals(
                "Test failed for params $input",
                output,
                subject.mapListToCalendarShift(
                    listShift = input[0],
                    calendarDayCount = input[1],
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // listShift, calendarDayCount

                // Days 0, should be impossible, check just in case.
                arrayOf(listOf(0, 0), 0),

                // Days 1
                arrayOf(listOf(-13, 1), -13),
                arrayOf(listOf(-2, 1), -2),
                arrayOf(listOf(-1, 1), -1),
                arrayOf(listOf(0, 1), 0),
                arrayOf(listOf(1, 1), 1),
                arrayOf(listOf(2, 1), 2),
                arrayOf(listOf(13, 1), 13),

                // Days 3
                arrayOf(listOf(-13, 3), -4),
                arrayOf(listOf(-6, 3), -2),
                arrayOf(listOf(-5, 3), -1),
                arrayOf(listOf(-4, 3), -1),
                arrayOf(listOf(-3, 3), -1),
                arrayOf(listOf(-2, 3), 0),
                arrayOf(listOf(-1, 3), 0),
                arrayOf(listOf(0, 3), 0),
                arrayOf(listOf(1, 3), 1),
                arrayOf(listOf(2, 3), 1),
                arrayOf(listOf(3, 3), 1),
                arrayOf(listOf(4, 3), 2),
                arrayOf(listOf(13, 3), 5),

                // Days 5
                arrayOf(listOf(-15, 5), -3),
                arrayOf(listOf(-14, 5), -2),
                arrayOf(listOf(-10, 5), -2),
                arrayOf(listOf(-9, 5), -1),
                arrayOf(listOf(-5, 5), -1),
                arrayOf(listOf(-4, 5), 0),
                arrayOf(listOf(-3, 5), 0),
                arrayOf(listOf(-2, 5), 0),
                arrayOf(listOf(-1, 5), 0),
                arrayOf(listOf(0, 5), 0),
                arrayOf(listOf(1, 5), 1),
                arrayOf(listOf(2, 5), 1),
                arrayOf(listOf(3, 5), 1),
                arrayOf(listOf(4, 5), 1),
                arrayOf(listOf(5, 5), 1),
                arrayOf(listOf(6, 5), 2),
                arrayOf(listOf(10, 5), 2),
                arrayOf(listOf(11, 5), 3),
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
            val subject = CalendarToListShiftMapper()

            assertEquals(
                "Test failed for params $input",
                output,
                subject.recalculateRangeOnCalendarViewSwitched(
                    currentPosition = input[0] as Int,
                    lastListPosition = input[1] as Int,
                    showCalendar = input[2] as Boolean,
                    daysInCalendar = input[3] as Int,
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // currentPosition, lastListPosition, showCalendar, daysInCalendar

                // Days 0, should be impossible, check just in case.
                arrayOf(listOf(0, 0, true, 0), 0),
                arrayOf(listOf(0, 0, false, 0), 0),

                // List to calendar
                arrayOf(listOf(-13, 0, true, 3), -4),
                arrayOf(listOf(0, 0, true, 3), 0),
                arrayOf(listOf(13, 0, true, 3), 5),
                arrayOf(listOf(-15, 0, true, 5), -3),
                arrayOf(listOf(0, 0, true, 5), 0),
                arrayOf(listOf(11, 0, true, 5), 3),

                // Calendar to list, can't restore last saved position
                arrayOf(listOf(-13, 999, false, 3), -39),
                arrayOf(listOf(0, 999, false, 3), 0),
                arrayOf(listOf(13, 999, false, 3), 39),
                arrayOf(listOf(-13, 999, false, 5), -65),
                arrayOf(listOf(0, 999, false, 5), 0),
                arrayOf(listOf(13, 999, false, 5), 65),

                // Calendar to list, can restore last saved position
                arrayOf(listOf(-13, -41, false, 3), -41),
                arrayOf(listOf(0, -1, false, 3), -1),
                arrayOf(listOf(13, 37, false, 3), 37),
                arrayOf(listOf(-13, -68, false, 5), -68),
                arrayOf(listOf(0, -3, false, 5), -3),
                arrayOf(listOf(13, 64, false, 5), 64),
            )
        }
    }

    @RunWith(Parameterized::class)
    class RecalculateRangeOnCalendarDaysChanged(
        private val input: List<Int>,
        private val output: Int,
    ) {

        @Test
        fun test() {
            val subject = CalendarToListShiftMapper()

            assertEquals(
                "Test failed for params $input",
                output,
                subject.recalculateRangeOnCalendarDaysChanged(
                    currentPosition = input[0],
                    currentDaysInCalendar = input[1],
                    newDaysInCalendar = input[2],
                ),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                // currentPosition, currentDaysInCalendar, newDaysInCalendar

                // Days 0, should be impossible, check just in case.
                arrayOf(listOf(0, 0, 0), 0),

                // From 1 to 3
                arrayOf(listOf(-13, 1, 3), -4),
                arrayOf(listOf(-3, 1, 3), -1),
                arrayOf(listOf(-2, 1, 3), 0),
                arrayOf(listOf(-1, 1, 3), 0),
                arrayOf(listOf(0, 1, 3), 0),
                arrayOf(listOf(1, 1, 3), 1),
                arrayOf(listOf(2, 1, 3), 1),
                arrayOf(listOf(3, 1, 3), 1),
                arrayOf(listOf(4, 1, 3), 2),
                arrayOf(listOf(13, 1, 3), 5),

                // From 3 to 1
                arrayOf(listOf(-13, 3, 1), -39),
                arrayOf(listOf(-2, 3, 1), -6),
                arrayOf(listOf(-1, 3, 1), -3),
                arrayOf(listOf(0, 3, 1), 0),
                arrayOf(listOf(1, 3, 1), 3),
                arrayOf(listOf(2, 3, 1), 6),
                arrayOf(listOf(13, 3, 1), 39),

                // From 3 to 7
                arrayOf(listOf(-13, 3, 7), -5),
                arrayOf(listOf(-2, 3, 7), 0),
                arrayOf(listOf(-1, 3, 7), 0),
                arrayOf(listOf(0, 3, 7), 0),
                arrayOf(listOf(1, 3, 7), 1),
                arrayOf(listOf(2, 3, 7), 1),
                arrayOf(listOf(13, 3, 7), 6),

                // From 7 to 3
                arrayOf(listOf(-13, 7, 3), -30),
                arrayOf(listOf(-2, 7, 3), -4),
                arrayOf(listOf(-1, 7, 3), -2),
                arrayOf(listOf(0, 7, 3), 0),
                arrayOf(listOf(1, 7, 3), 3),
                arrayOf(listOf(2, 7, 3), 5),
                arrayOf(listOf(13, 7, 3), 31),
            )
        }
    }
}