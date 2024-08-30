package com.example.util.simpletimetracker.core.view.dayCalendar

data class DayCalendarViewData(
    val data: List<Point>,
) {

    data class Point(
        val start: Long,
        val end: Long,
        val data: Data,
    ) {

        data class Data(
            val color: Int,
        )
    }
}