package com.example.util.simpletimetracker.domain.extension

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import java.util.concurrent.TimeUnit
import java.time.DayOfWeek as JavaDayOfWeek

fun Range?.orEmpty(): Range = this ?: Range(0, 0)

fun RecordBase.toRange(): Range {
    return Range(timeStarted = timeStarted, timeEnded = timeEnded)
}

fun JavaDayOfWeek.toDomainDayOfWeek(): DayOfWeek {
    return when (this) {
        JavaDayOfWeek.MONDAY -> DayOfWeek.MONDAY
        JavaDayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
        JavaDayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
        JavaDayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
        JavaDayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
        JavaDayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
        JavaDayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
    }
}

fun Long.isValidTimeOfDay(): Boolean {
    return this in 0 until TimeUnit.DAYS.toMillis(1)
}