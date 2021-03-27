package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RangeLength
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TimeMapper @Inject constructor(
    private val resourceRepo: ResourceRepo
) {

    private val calendar = Calendar.getInstance()

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val timeFormatMilitary = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val dateTimeFormat = SimpleDateFormat("MMM d h:mm a", Locale.getDefault())
    private val dateTimeFormatMilitary = SimpleDateFormat("MMM d HH:mm", Locale.getDefault())

    private val dateTimeYearFormat = SimpleDateFormat("MMM d yyyy h:mm a", Locale.getDefault())
    private val dateTimeYearFormatMilitary = SimpleDateFormat("MMM d yyyy HH:mm", Locale.getDefault())

    private val dateYearFormat = SimpleDateFormat("MMM d yyyy", Locale.getDefault())
    private val shortDayFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
    private val shortMonthFormat = SimpleDateFormat("MMM", Locale.getDefault())
    private val shortYearFormat = SimpleDateFormat("yy", Locale.getDefault())

    private val dayTitleFormat = SimpleDateFormat("E, MMM d", Locale.getDefault())
    private val weekTitleFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val monthTitleFormat = SimpleDateFormat("MMMM", Locale.getDefault())
    private val yearTitleFormat = SimpleDateFormat("yyyy", Locale.getDefault())

    // 12:21
    fun formatTime(time: Long, useMilitaryTime: Boolean): String {
        return if (useMilitaryTime) {
            timeFormatMilitary
        } else {
            timeFormat
        }.format(time)
    }

    // Mar 11 12:21
    fun formatDateTime(time: Long, useMilitaryTime: Boolean): String {
        return if (useMilitaryTime) {
            dateTimeFormatMilitary
        } else {
            dateTimeFormat
        }.format(time)
    }

    // Mar 12 2021 12:21
    fun formatDateTimeYear(time: Long, useMilitaryTime: Boolean): String {
        return if (useMilitaryTime) {
            dateTimeYearFormatMilitary
        } else {
            dateTimeYearFormat
        }.format(time)
    }

    // Mar 12 2021
    fun formatDateYear(time: Long): String {
        return dateYearFormat.format(time)
    }

    // 12.03
    fun formatShortDay(time: Long): String {
        return shortDayFormat.format(time)
    }

    // Mar
    fun formatShortMonth(time: Long): String {
        return shortMonthFormat.format(time)
    }

    // 21
    fun formatShortYear(time: Long): String {
        return shortYearFormat.format(time)
    }

    // 1h 7m
    fun formatInterval(interval: Long): String =
        formatInterval(interval, withSeconds = false)

    // 1h 7m 21s
    fun formatIntervalWithSeconds(interval: Long): String =
        formatInterval(interval, withSeconds = true)

    fun toTimestampShifted(rangesFromToday: Int, range: RangeLength): Long {
        val calendarStep = when (range) {
            RangeLength.DAY -> Calendar.DAY_OF_YEAR
            RangeLength.WEEK -> Calendar.WEEK_OF_YEAR
            RangeLength.MONTH -> Calendar.MONTH
            RangeLength.YEAR -> Calendar.YEAR
            RangeLength.ALL -> return 0
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

    fun toTimestampShift(toTime: Long, range: RangeLength): Long {
        val calendarStep = when (range) {
            RangeLength.DAY -> Calendar.DAY_OF_YEAR
            RangeLength.WEEK -> Calendar.WEEK_OF_YEAR
            RangeLength.MONTH -> Calendar.MONTH
            RangeLength.YEAR -> Calendar.YEAR
            RangeLength.ALL -> return 0
        }

        val current = System.currentTimeMillis()
        var result = 0L

        calendar.timeInMillis = toTime
        result += if (calendarStep == Calendar.WEEK_OF_YEAR && isFirstWeekOfNextYear(calendar)) {
            calendar.getActualMaximum(Calendar.WEEK_OF_YEAR) + 1
        } else {
            calendar.get(calendarStep)
        }

        if (calendarStep == Calendar.MONTH) result++

        calendar.timeInMillis = current
        result -= if (calendarStep == Calendar.WEEK_OF_YEAR && isFirstWeekOfNextYear(calendar)) {
            calendar.getActualMaximum(Calendar.WEEK_OF_YEAR) + 1
        } else {
            calendar.get(calendarStep)
        }
        if (calendarStep == Calendar.MONTH) result--

        if (calendarStep == Calendar.YEAR) return result

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

    // Tue, Mar 12
    fun toDayTitle(daysFromToday: Int): String {
        return when (daysFromToday) {
            -1 -> resourceRepo.getString(R.string.title_yesterday)
            0 -> resourceRepo.getString(R.string.title_today)
            1 -> resourceRepo.getString(R.string.title_tomorrow)
            else -> toDayDateTitle(daysFromToday)
        }
    }

    // Mar 1 - Mar 7
    fun toWeekTitle(weeksFromToday: Int): String {
        return when (weeksFromToday) {
            0 -> resourceRepo.getString(R.string.title_this_week)
            else -> toWeekDateTitle(weeksFromToday)
        }
    }

    // March
    fun toMonthTitle(monthsFromToday: Int): String {
        return when (monthsFromToday) {
            0 -> resourceRepo.getString(R.string.title_this_month)
            else -> toMonthDateTitle(monthsFromToday)
        }
    }

    // 2021
    fun toYearTitle(yearsFromToday: Int): String {
        return when (yearsFromToday) {
            0 -> resourceRepo.getString(R.string.title_this_year)
            else -> toYearDateTitle(yearsFromToday)
        }
    }

    fun sameDay(date1: Long, date2: Long): Boolean {
        calendar.apply { timeInMillis = date1 }
        val year1: Int = calendar.get(Calendar.YEAR)
        val day1: Int = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.apply { timeInMillis = date2 }
        val year2: Int = calendar.get(Calendar.YEAR)
        val day2: Int = calendar.get(Calendar.DAY_OF_YEAR)

        return year1 == year2 && day1 == day2
    }

    fun sameHour(date1: Long, date2: Long): Boolean {
        calendar.apply { timeInMillis = date1 }
        val year1: Int = calendar.get(Calendar.YEAR)
        val day1: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val hour1: Int = calendar.get(Calendar.HOUR_OF_DAY)

        calendar.apply { timeInMillis = date2 }
        val year2: Int = calendar.get(Calendar.YEAR)
        val day2: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val hour2: Int = calendar.get(Calendar.HOUR_OF_DAY)

        return year1 == year2 && day1 == day2 && hour1 == hour2
    }

    fun formatDuration(interval: Long): String {
        val hourString = resourceRepo.getString(R.string.time_hour)
        val minuteString = resourceRepo.getString(R.string.time_minute)
        val secondString = resourceRepo.getString(R.string.time_second)

        val hr: Long = TimeUnit.SECONDS.toHours(
            interval
        )
        val min: Long = TimeUnit.SECONDS.toMinutes(
            interval - TimeUnit.HOURS.toSeconds(hr)
        )
        val sec: Long = TimeUnit.SECONDS.toSeconds(
            interval - TimeUnit.HOURS.toSeconds(hr) - TimeUnit.MINUTES.toSeconds(min)
        )

        val hrString = "$hr$hourString"
        val minString = min.toString().let {
            if (hr != 0L) it.padStart(2, '0') else it
        } + minuteString
        val secString = sec.toString().let {
            if (hr != 0L || min != 0L) it.padStart(2, '0') else it
        } + secondString

        var res = ""
        if (hr != 0L) res += hrString
        if (hr != 0L && min != 0L) res += " "
        if (min != 0L) res += minString
        if ((hr != 0L || min != 0L) && sec != 0L) res += " "
        if (sec != 0L) res += secString

        return res
    }

    fun getRangeStartAndEnd(rangeLength: RangeLength, shift: Int): Pair<Long, Long> {
        val rangeStart: Long
        val rangeEnd: Long
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (rangeLength) {
            RangeLength.DAY -> {
                calendar.add(Calendar.DATE, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis
            }
            RangeLength.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.add(Calendar.DATE, shift * 7)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 7) }.timeInMillis
            }
            RangeLength.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis
            }
            RangeLength.YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.add(Calendar.YEAR, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.YEAR, 1) }.timeInMillis
            }
            RangeLength.ALL -> {
                rangeStart = 0L
                rangeEnd = 0L
            }
        }

        return rangeStart to rangeEnd
    }

    private fun formatInterval(interval: Long, withSeconds: Boolean): String {
        val hourString = resourceRepo.getString(R.string.time_hour)
        val minuteString = resourceRepo.getString(R.string.time_minute)
        val secondString = resourceRepo.getString(R.string.time_second)

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
        if (hr != 0L) res += "$hr$hourString"
        if (hr != 0L || min != 0L || !withSeconds) res += " $min$minuteString"
        if (withSeconds) res += " $sec$secondString"

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

    private fun toYearDateTitle(yearsFromToday: Int): String {
        calendar.apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.YEAR, yearsFromToday)
        }

        return yearTitleFormat.format(calendar.timeInMillis)
    }

    private fun isFirstWeekOfNextYear(calendar: Calendar): Boolean {
        return calendar.get(Calendar.WEEK_OF_YEAR) == 1 &&
            calendar.get(Calendar.MONTH) == calendar.getActualMaximum(Calendar.MONTH)
    }
}