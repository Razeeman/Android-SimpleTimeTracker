package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TimeMapper @Inject constructor(
    private val resourceRepo: ResourceRepo
) {

    enum class Range {
        DAY, WEEK, MONTH
    }

    private val calendar = Calendar.getInstance()
    private val timeFormat = SimpleDateFormat("kk:mm", Locale.US)
    private val dateFormat = SimpleDateFormat("MMM d kk:mm", Locale.US)
    private val dateYearFormat = SimpleDateFormat("MMM d yyyy kk:mm", Locale.US)
    private val dayTitleFormat = SimpleDateFormat("E, MMM d", Locale.US)
    private val weekTitleFormat = SimpleDateFormat("MMM d", Locale.US)
    private val monthTitleFormat = SimpleDateFormat("MMMM", Locale.US)

    fun formatTime(time: Long): String {
        return timeFormat.format(time)
    }

    fun formatDateTime(time: Long): String {
        return dateFormat.format(time)
    }

    fun formatDateYearTime(time: Long): String {
        return dateYearFormat.format(time)
    }

    fun formatInterval(interval: Long): String =
        formatInterval(interval, withSeconds = false)

    fun formatIntervalWithSeconds(interval: Long): String =
        formatInterval(interval, withSeconds = true)

    fun toTimestampShifted(rangesFromToday: Int, range: Range): Long {
        val calendarStep = when (range) {
            Range.DAY -> Calendar.DAY_OF_YEAR
            Range.WEEK -> Calendar.WEEK_OF_YEAR
            Range.MONTH -> Calendar.MONTH
        }

        return if (rangesFromToday != 0) {
            calendar
                .apply {
                    timeInMillis = System.currentTimeMillis()
                    add(calendarStep, rangesFromToday)
                }
                .timeInMillis
        } else {
            return System.currentTimeMillis()
        }
    }

    fun toTimestampShift(toTime: Long, range: Range): Long {
        val calendarStep = when (range) {
            Range.DAY -> Calendar.DAY_OF_YEAR
            Range.WEEK -> Calendar.WEEK_OF_YEAR
            Range.MONTH -> Calendar.MONTH
        }

        val current = System.currentTimeMillis()
        var result = 0L

        calendar.timeInMillis = toTime
        result += calendar.get(calendarStep)
        if (calendarStep == Calendar.MONTH) result++

        calendar.timeInMillis = current
        result -= calendar.get(calendarStep)
        if (calendarStep == Calendar.MONTH) result--

        val yearInFuture: Int
        val shiftDirection: Int
        if (toTime < current) {
            yearInFuture = calendar.apply { timeInMillis = current }.get(Calendar.YEAR)
            calendar.apply { timeInMillis = toTime }
            shiftDirection = 1
        } else {
            yearInFuture = calendar.apply { timeInMillis = toTime }.get(Calendar.YEAR)
            calendar.apply { timeInMillis = current }
            shiftDirection = -1
        }

        while (calendar.get(Calendar.YEAR) != yearInFuture) {
            result -= shiftDirection * calendar.getActualMaximum(calendarStep)
            if (calendarStep == Calendar.MONTH) result -= shiftDirection
            calendar.add(Calendar.YEAR, 1)
        }

        return result
    }

    fun toDayTitle(daysFromToday: Int): String {
        return when (daysFromToday) {
            -1 -> resourceRepo.getString(R.string.title_yesterday)
            0 -> resourceRepo.getString(R.string.title_today)
            1 -> resourceRepo.getString(R.string.title_tomorrow)
            else -> toDayDateTitle(daysFromToday)
        }
    }

    fun toWeekTitle(weeksFromToday: Int): String {
        return when (weeksFromToday) {
            0 -> resourceRepo.getString(R.string.title_this_week)
            else -> toWeekDateTitle(weeksFromToday)
        }
    }

    fun toMonthTitle(monthsFromToday: Int): String {
        return when (monthsFromToday) {
            0 -> resourceRepo.getString(R.string.title_this_month)
            else -> toMonthDateTitle(monthsFromToday)
        }
    }

    private fun formatInterval(interval: Long, withSeconds: Boolean): String {
        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr)
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)
        )

        var res = ""
        if (hr != 0L) res += "${hr}h"
        if (hr != 0L || min != 0L || !withSeconds) res += " ${min}m"
        if (withSeconds) res += " ${sec}s"

        return res
    }

    private fun toDayDateTitle(daysFromToday: Int): String {
        calendar.apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, daysFromToday)
        }

        return dayTitleFormat.format(calendar.timeInMillis)
    }

    private fun toWeekDateTitle(weeksFromToday: Int): String {
        calendar.apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            add(Calendar.DATE, weeksFromToday * 7)
        }
        val rangeStart = calendar.timeInMillis
        val rangeEnd = calendar.apply { add(Calendar.DATE, 6) }.timeInMillis

        return weekTitleFormat.format(rangeStart) + " - " + weekTitleFormat.format(rangeEnd)
    }

    private fun toMonthDateTitle(monthsFromToday: Int): String {
        calendar.apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MONTH, monthsFromToday)
        }

        return monthTitleFormat.format(calendar.timeInMillis)
    }
}