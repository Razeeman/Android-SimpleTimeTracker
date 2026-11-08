package com.example.util.simpletimetracker.core.extension

import java.util.Calendar
import android.icu.util.Calendar as IcuCalendar

fun Calendar.setToStartOfDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun IcuCalendar.setToStartOfDay() {
    set(IcuCalendar.HOUR_OF_DAY, 0)
    set(IcuCalendar.MINUTE, 0)
    set(IcuCalendar.SECOND, 0)
    set(IcuCalendar.MILLISECOND, 0)
}

fun Calendar.setWeekToFirstDay() {
    val another = Calendar.getInstance(timeZone)
    // Match week rules to avoid jumping forward into a skipped date (e.g. Apia, Dec 30, 2011),
    // where subtracting seven days afterward can land on the wrong weekday.
    another.firstDayOfWeek = this@setWeekToFirstDay.firstDayOfWeek
    another.timeInMillis = timeInMillis

    val currentTime = another.timeInMillis
    // Setting DAY_OF_WEEK have a weird behaviour so as if after that another field is set -
    // it would reset to current day. Use another calendar to manipulate day of week and get its time.
    another.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    // If went to future - go back a week
    if (another.timeInMillis > currentTime) {
        another.add(Calendar.DATE, -7)
    }

    timeInMillis = another.timeInMillis
}

fun Calendar.shift(shift: Long): Calendar {
    if (shift == 0L) return this
    // Used for start of day shift on dst change.
    // Example, shift 6h:
    // before 2023-03-26T00:00+01:00[Europe/Amsterdam] DST_OFFSET = 0
    // after 2023-03-26T07:00+02:00[Europe/Amsterdam] DST_OFFSET = 3600000
    // need to compensate one hour to preserve correct start of day.
    var dstOffsetBefore = get(Calendar.DST_OFFSET)
    timeInMillis += shift
    var dstOffsetAfter = get(Calendar.DST_OFFSET)

    // Compensate.
    val dstChange = dstOffsetBefore - dstOffsetAfter
    timeInMillis += dstChange

    // If dst fix causes another dst change - rollback,
    // it means timestamp after shift falls right on the dst change period,
    // for example 30 March 2025 Germany 02:00 -> 03:00,
    // otherwise 00:00 + 2h would be 01:00 after compensation,
    // and 01:00 - 2h would be different date.
    if (dstOffsetAfter != get(Calendar.DST_OFFSET)) {
        timeInMillis -= dstChange
    }
    return this
}

/** Applies the same DST compensation and gap rollback as [Calendar.shift]. */
fun IcuCalendar.shift(shift: Long): IcuCalendar {
    if (shift == 0L) return this
    val dstOffsetBefore = get(IcuCalendar.DST_OFFSET)
    timeInMillis += shift
    val dstOffsetAfter = get(IcuCalendar.DST_OFFSET)

    val dstChange = dstOffsetBefore - dstOffsetAfter
    timeInMillis += dstChange

    // Roll back compensation if it crosses the DST transition again.
    if (dstOffsetAfter != get(IcuCalendar.DST_OFFSET)) {
        timeInMillis -= dstChange
    }
    return this
}

fun Calendar.shiftTimeStamp(timestamp: Long, shift: Long): Long {
    if (shift == 0L) return timestamp
    timeInMillis = timestamp
    shift(shift)
    return timeInMillis
}

fun IcuCalendar.shiftTimeStamp(timestamp: Long, shift: Long): Long {
    if (shift == 0L) return timestamp
    timeInMillis = timestamp
    shift(shift)
    return timeInMillis
}

fun Long.shiftTimeStamp(shift: Long): Long {
    if (shift == 0L) return this
    return Calendar.getInstance().shiftTimeStamp(timestamp = this, shift = shift)
}