package com.example.util.simpletimetracker.feature_records.customView

import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData

data class RecordsCalendarViewData(
    val currentTime: Long?,
    val startOfDayShift: Long,
    val points: List<Point>,
    val reverseOrder: Boolean,
) {

    data class Point(
        val start: Long,
        val end: Long,
        val data: RecordViewData
    )
}