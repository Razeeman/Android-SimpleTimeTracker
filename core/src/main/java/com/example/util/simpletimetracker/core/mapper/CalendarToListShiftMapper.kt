package com.example.util.simpletimetracker.core.mapper

import javax.inject.Inject
import kotlin.math.ceil

class CalendarToListShiftMapper @Inject constructor() {

    fun mapCalendarToListShift(calendarShift: Int, calendarDayCount: Int): CalendarRange {
        if (calendarDayCount == 0) return CalendarRange(0, 0)

        val end = calendarShift * calendarDayCount
        val start = end - (calendarDayCount - 1)

        return CalendarRange(start, end)
    }

    fun mapListToCalendarShift(listShift: Int, calendarDayCount: Int): Int {
        if (calendarDayCount == 0) return 0

        return ceil(listShift.toFloat() / calendarDayCount).toInt()
    }

    fun recalculateRangeOnCalendarViewSwitched(
        currentPosition: Int,
        lastListPosition: Int,
        showCalendar: Boolean,
        daysInCalendar: Int,
    ): Int {
        return if (showCalendar) {
            mapListToCalendarShift(
                listShift = currentPosition,
                calendarDayCount = daysInCalendar,
            )
        } else {
            val calendarRange = mapCalendarToListShift(
                calendarShift = currentPosition,
                calendarDayCount = daysInCalendar,
            )
            if (lastListPosition in (calendarRange.start..calendarRange.end)) {
                lastListPosition
            } else {
                calendarRange.end
            }
        }
    }

    fun recalculateRangeOnCalendarDaysChanged(
        currentPosition: Int,
        currentDaysInCalendar: Int,
        newDaysInCalendar: Int,
    ): Int {
        // Find another range that contains last day of current range.
        val listPosition = mapCalendarToListShift(
            calendarShift = currentPosition,
            calendarDayCount = currentDaysInCalendar,
        ).end
        return mapListToCalendarShift(
            listShift = listPosition,
            calendarDayCount = newDaysInCalendar,
        )
    }

    data class CalendarRange(
        val start: Int,
        val end: Int,
    )
}