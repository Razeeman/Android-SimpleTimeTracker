package com.example.util.simpletimetracker.domain.utils

import com.example.util.simpletimetracker.domain.extension.isValidTimeOfDay
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LocalDateMapper @Inject constructor() {

    fun resolveDateTime(
        date: LocalDate,
        timeOfDayMillis: Long,
        timeZone: TimeZone,
    ): Long? {
        if (!timeOfDayMillis.isValidTimeOfDay()) return null

        return try {
            val time = LocalTime.ofNanoOfDay(TimeUnit.MILLISECONDS.toNanos(timeOfDayMillis))

            // atZone moves times in a DST gap forward by the gap.
            // Select the earlier instant when the local time occurs twice during an overlap.
            date.atTime(time)
                .atZone(timeZone.toZoneId())
                .withEarlierOffsetAtOverlap()
                .toInstant()
                .toEpochMilli()
        } catch (_: DateTimeException) {
            null
        } catch (_: ArithmeticException) {
            null
        }
    }
}