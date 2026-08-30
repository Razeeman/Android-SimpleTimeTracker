package com.example.util.simpletimetracker.domain.extension

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import java.time.Instant
import java.time.ZonedDateTime
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.time.DayOfWeek as JavaDayOfWeek

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

fun Long.toLocalDateTime(timeZone: TimeZone): ZonedDateTime {
    return Instant.ofEpochMilli(this).atZone(timeZone.toZoneId())
}