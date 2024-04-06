package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.core.extension.shift
import com.example.util.simpletimetracker.core.provider.CurrentTimestampProvider
import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TimeMapper @Inject constructor(
    localeProvider: LocaleProvider,
    private val resourceRepo: ResourceRepo,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    private val locale: Locale by lazy { localeProvider.get() }

    private val timeFormat by lazy { SimpleDateFormat("h:mm a", locale) }
    private val timeFormatWithSeconds by lazy { SimpleDateFormat("h:mm:ss a", locale) }
    private val timeFormatMilitary by lazy { SimpleDateFormat("HH:mm", locale) }
    private val timeFormatMilitaryWithSeconds by lazy { SimpleDateFormat("HH:mm:ss", locale) }

    private val dateTimeFormat by lazy { SimpleDateFormat("MMM d h:mm a", locale) }
    private val dateTimeFormatWithSeconds by lazy { SimpleDateFormat("MMM d h:mm:ss a", locale) }
    private val dateTimeFormatMilitary by lazy { SimpleDateFormat("MMM d HH:mm", locale) }
    private val dateTimeFormatMilitaryWithSeconds by lazy { SimpleDateFormat("MMM d HH:mm:ss", locale) }

    private val dateTimeYearFormat by lazy { SimpleDateFormat("MMM d yyyy h:mm a", locale) }
    private val dateTimeYearFormatMilitary by lazy { SimpleDateFormat("MMM d yyyy HH:mm", locale) }

    private val dayDateYearFormat by lazy { SimpleDateFormat("E, MMM d yyyy", locale) }
    private val dateYearFormat by lazy { SimpleDateFormat("MMM d yyyy", locale) }
    private val shortDayFormatMMDD by lazy { SimpleDateFormat("MM.dd", locale) }
    private val shortDayFormatDDMM by lazy { SimpleDateFormat("dd.MM", locale) }
    private val shortMonthFormat by lazy { SimpleDateFormat("MMM", locale) }
    private val shortYearFormat by lazy { SimpleDateFormat("yy", locale) }

    private val dayTitleFormat by lazy { SimpleDateFormat("E, MMM d", locale) }
    private val weekTitleFormat by lazy { SimpleDateFormat("MMM d", locale) }
    private val monthTitleFormat by lazy { SimpleDateFormat("MMMM", locale) }
    private val yearTitleFormat by lazy { SimpleDateFormat("yyyy", locale) }

    private val lock = Any()

    // 12:21
    fun formatTime(
        time: Long,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
    ): String = synchronized(lock) {
        return if (useMilitaryTime) {
            if (showSeconds) timeFormatMilitaryWithSeconds else timeFormatMilitary
        } else {
            if (showSeconds) timeFormatWithSeconds else timeFormat
        }.format(time)
    }

    // Mar 11 12:21
    fun formatDateTime(
        time: Long,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
    ): String = synchronized(lock) {
        return if (useMilitaryTime) {
            if (showSeconds) dateTimeFormatMilitaryWithSeconds else dateTimeFormatMilitary
        } else {
            if (showSeconds) dateTimeFormatWithSeconds else dateTimeFormat
        }.format(time)
    }

    // Mar 12 2021 12:21
    fun formatDateTimeYear(time: Long, useMilitaryTime: Boolean): String = synchronized(lock) {
        return if (useMilitaryTime) {
            dateTimeYearFormatMilitary
        } else {
            dateTimeYearFormat
        }.format(time)
    }

    // Mar 12 2021
    fun formatDateYear(time: Long): String = synchronized(lock) {
        return dateYearFormat.format(time)
    }

    // Tue, Mar 12 2021
    fun formatDayDateYear(time: Long): String = synchronized(lock) {
        return dayDateYearFormat.format(time)
    }

    // 12.03
    fun formatShortDay(time: Long, useMonthDayTimeFormat: Boolean): String = synchronized(lock) {
        if (useMonthDayTimeFormat) {
            return shortDayFormatMMDD.format(time)
        } else {
            return shortDayFormatDDMM.format(time)
        }
    }

    // Mar
    fun formatShortMonth(time: Long): String = synchronized(lock) {
        return shortMonthFormat.format(time)
    }

    // 21
    fun formatShortYear(time: Long): String = synchronized(lock) {
        return shortYearFormat.format(time)
    }

    fun toTimestampShifted(rangesFromToday: Int, range: RangeLength): Long {
        val calendarStep = when (range) {
            is RangeLength.Day -> Calendar.DAY_OF_YEAR
            is RangeLength.Week -> Calendar.WEEK_OF_YEAR
            is RangeLength.Month -> Calendar.MONTH
            is RangeLength.Year -> Calendar.YEAR
            is RangeLength.All -> return 0
            is RangeLength.Custom -> return 0
            is RangeLength.Last -> return 0
        }

        return if (rangesFromToday != 0) {
            Calendar.getInstance()
                .apply {
                    timeInMillis = System.currentTimeMillis()
                    add(calendarStep, rangesFromToday)
                }
                .timeInMillis
        } else {
            return System.currentTimeMillis()
        }
    }

    fun toTimestampShift(
        fromTime: Long = System.currentTimeMillis(),
        toTime: Long,
        range: RangeLength,
        firstDayOfWeek: DayOfWeek,
    ): Long {
        val calendarStep = when (range) {
            is RangeLength.Day -> Calendar.DAY_OF_YEAR
            is RangeLength.Week -> Calendar.WEEK_OF_YEAR
            is RangeLength.Month -> Calendar.MONTH
            is RangeLength.Year -> Calendar.YEAR
            is RangeLength.All -> return 0
            is RangeLength.Custom -> return 0
            is RangeLength.Last -> return 0
        }

        val calendar = Calendar.getInstance()
        var result = 0L

        calendar.firstDayOfWeek = toCalendarDayOfWeek(firstDayOfWeek)
        calendar.timeInMillis = toTime
        result += if (calendarStep == Calendar.WEEK_OF_YEAR && isFirstWeekOfNextYear(calendar)) {
            calendar.getActualMaximum(Calendar.WEEK_OF_YEAR) + 1
        } else {
            calendar.get(calendarStep)
        }

        if (calendarStep == Calendar.MONTH) result++

        calendar.timeInMillis = fromTime
        result -= if (calendarStep == Calendar.WEEK_OF_YEAR && isFirstWeekOfNextYear(calendar)) {
            calendar.getActualMaximum(Calendar.WEEK_OF_YEAR) + 1
        } else {
            calendar.get(calendarStep)
        }
        if (calendarStep == Calendar.MONTH) result--

        if (calendarStep == Calendar.YEAR) return result

        val yearInFuture: Int
        val shiftDirection: Int
        if (toTime < fromTime) {
            yearInFuture = calendar.apply { timeInMillis = fromTime }.get(Calendar.YEAR)
            calendar.apply { timeInMillis = toTime }
            shiftDirection = 1
        } else {
            yearInFuture = calendar.apply { timeInMillis = toTime }.get(Calendar.YEAR)
            calendar.apply { timeInMillis = fromTime }
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
    fun toDayTitle(
        daysFromToday: Int,
        startOfDayShift: Long,
    ): String {
        return when (daysFromToday) {
            -1 -> resourceRepo.getString(R.string.title_yesterday)
            0 -> resourceRepo.getString(R.string.title_today)
            1 -> resourceRepo.getString(R.string.title_tomorrow)
            else -> toDayDateTitle(daysFromToday, startOfDayShift)
        }
    }

    // Mar 1 - Mar 7
    fun toWeekTitle(
        weeksFromToday: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): String {
        return when (weeksFromToday) {
            0 -> resourceRepo.getString(R.string.title_this_week)
            else -> toWeekDateTitle(weeksFromToday, startOfDayShift, firstDayOfWeek)
        }
    }

    // March
    fun toMonthTitle(
        monthsFromToday: Int,
        startOfDayShift: Long,
    ): String {
        return when (monthsFromToday) {
            0 -> resourceRepo.getString(R.string.title_this_month)
            else -> toMonthDateTitle(monthsFromToday, startOfDayShift)
        }
    }

    // 2021
    fun toYearTitle(
        yearsFromToday: Int,
        startOfDayShift: Long,
    ): String {
        return when (yearsFromToday) {
            0 -> resourceRepo.getString(R.string.title_this_year)
            else -> toYearDateTitle(yearsFromToday, startOfDayShift)
        }
    }

    fun toShortDayOfWeekName(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.SUNDAY -> R.string.day_of_week_sunday
            DayOfWeek.MONDAY -> R.string.day_of_week_monday
            DayOfWeek.TUESDAY -> R.string.day_of_week_tuesday
            DayOfWeek.WEDNESDAY -> R.string.day_of_week_wednesday
            DayOfWeek.THURSDAY -> R.string.day_of_week_thursday
            DayOfWeek.FRIDAY -> R.string.day_of_week_friday
            DayOfWeek.SATURDAY -> R.string.day_of_week_saturday
        }.let(resourceRepo::getString)
    }

    fun toCalendarDayOfWeek(dayOfWeek: DayOfWeek): Int {
        return when (dayOfWeek) {
            DayOfWeek.SUNDAY -> Calendar.SUNDAY
            DayOfWeek.MONDAY -> Calendar.MONDAY
            DayOfWeek.TUESDAY -> Calendar.TUESDAY
            DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
            DayOfWeek.THURSDAY -> Calendar.THURSDAY
            DayOfWeek.FRIDAY -> Calendar.FRIDAY
            DayOfWeek.SATURDAY -> Calendar.SATURDAY
        }
    }

    fun toDayOfWeek(dayOfWeek: Int): DayOfWeek {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            else -> DayOfWeek.SUNDAY
        }
    }

    fun sameDay(date1: Long, date2: Long, calendar: Calendar): Boolean {
        calendar.apply { timeInMillis = date1 }
        val year1: Int = calendar.get(Calendar.YEAR)
        val day1: Int = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.apply { timeInMillis = date2 }
        val year2: Int = calendar.get(Calendar.YEAR)
        val day2: Int = calendar.get(Calendar.DAY_OF_YEAR)

        return year1 == year2 && day1 == day2
    }

    fun sameHour(date1: Long, date2: Long, calendar: Calendar): Boolean {
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

    /**
     * @param interval in seconds.
     */
    fun formatDuration(interval: Long): String {
        val hourString = resourceRepo.getString(R.string.time_hour)
        val minuteString = resourceRepo.getString(R.string.time_minute)
        val secondString = resourceRepo.getString(R.string.time_second)

        val hr: Long = TimeUnit.SECONDS.toHours(
            interval,
        )
        val min: Long = TimeUnit.SECONDS.toMinutes(
            interval - TimeUnit.HOURS.toSeconds(hr),
        )
        val sec: Long = TimeUnit.SECONDS.toSeconds(
            interval - TimeUnit.HOURS.toSeconds(hr) - TimeUnit.MINUTES.toSeconds(min),
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
        if ((hr == 0L && min == 0L) || sec != 0L) res += secString

        return res
    }

    fun getRangeStartAndEnd(
        rangeLength: RangeLength,
        shift: Int,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): Range {
        val dayOfWeek = toCalendarDayOfWeek(firstDayOfWeek)
        val rangeStart: Long
        val rangeEnd: Long
        val calendar = Calendar.getInstance().apply {
            this.firstDayOfWeek = dayOfWeek
            timeInMillis = currentTimestampProvider.get()

            shift(-startOfDayShift)
            when (rangeLength) {
                is RangeLength.Week -> setWeekToFirstDay()
                is RangeLength.Month -> set(Calendar.DAY_OF_MONTH, 1)
                is RangeLength.Year -> set(Calendar.DAY_OF_YEAR, 1)
                else -> {
                    // Do nothing
                }
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            shift(startOfDayShift)
        }

        when (rangeLength) {
            is RangeLength.Day -> {
                calendar.add(Calendar.DATE, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis
            }

            is RangeLength.Week -> {
                calendar.add(Calendar.DATE, shift * 7)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 7) }.timeInMillis
            }

            is RangeLength.Month -> {
                calendar.apply {
                    shift(-startOfDayShift)
                    add(Calendar.MONTH, shift)
                    shift(+startOfDayShift)
                }
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply {
                    shift(-startOfDayShift)
                    add(Calendar.MONTH, 1)
                    shift(startOfDayShift)
                }.timeInMillis
            }

            is RangeLength.Year -> {
                calendar.apply {
                    shift(-startOfDayShift)
                    add(Calendar.YEAR, shift)
                    shift(startOfDayShift)
                }
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply {
                    shift(-startOfDayShift)
                    add(Calendar.YEAR, 1)
                    shift(startOfDayShift)
                }.timeInMillis
            }

            is RangeLength.All -> {
                rangeStart = 0L
                rangeEnd = 0L
            }

            is RangeLength.Custom -> {
                rangeStart = rangeLength.range.timeStarted
                rangeEnd = rangeLength.range.timeEnded
            }

            is RangeLength.Last -> {
                rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis
                rangeStart = calendar.apply { add(Calendar.DATE, -rangeLength.DAYS) }.timeInMillis
            }
        }

        return Range(rangeStart, rangeEnd)
    }

    fun getActualMaximum(
        startDate: Long,
        field: Int,
        firstDayOfWeek: DayOfWeek,
    ): Int {
        val dayOfWeek = toCalendarDayOfWeek(firstDayOfWeek)
        return Calendar.getInstance()
            .apply { timeInMillis = startDate }
            .apply { this.firstDayOfWeek = dayOfWeek }
            .getActualMaximum(field)
    }

    /**
     * @param forceSeconds - true 1h 7m 21s, false 1h 7m
     * @param useProportionalMinutes - true 1.25h
     */
    fun formatInterval(
        interval: Long,
        forceSeconds: Boolean,
        useProportionalMinutes: Boolean,
    ): String {
        val hourString = resourceRepo.getString(R.string.time_hour)
        val minuteString = resourceRepo.getString(R.string.time_minute)
        val secondString = resourceRepo.getString(R.string.time_second)

        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval,
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr),
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min),
        )

        if (useProportionalMinutes) {
            return formatIntervalProportional(hr, min)
        }

        val willShowHours = hr != 0L
        val willShowMinutes = willShowHours || min != 0L
        val willShowSeconds = (!willShowHours && !willShowMinutes) || forceSeconds

        var res = ""
        if (willShowHours) res += "$hr$hourString "
        if (willShowMinutes) res += "$min$minuteString"
        if (willShowMinutes && willShowSeconds) res += " "
        if (willShowSeconds) res += "$sec$secondString"

        return res
    }

    fun formatIntervalAdjusted(
        timeStarted: Long,
        timeEnded: Long,
        showSeconds: Boolean,
        useProportionalMinutes: Boolean,
    ): String {
        val duration = timeEnded - timeStarted
        val interval = formatInterval(
            interval = duration,
            forceSeconds = showSeconds,
            useProportionalMinutes = useProportionalMinutes,
        )

        return if (
            showSeconds ||
            useProportionalMinutes ||
            duration < MINUTES_IN_MILLIS
        ) {
            interval
        } else {
            val adjustedTimeStarted = timeStarted / MINUTES_IN_MILLIS * MINUTES_IN_MILLIS
            val adjustedTimeEnded = timeEnded / MINUTES_IN_MILLIS * MINUTES_IN_MILLIS
            val adjustedInterval = formatInterval(
                interval = adjustedTimeEnded - adjustedTimeStarted,
                forceSeconds = false,
                useProportionalMinutes = false,
            )
            if (adjustedInterval != interval) {
                "~$adjustedInterval"
            } else {
                interval
            }
        }
    }

    private fun formatIntervalProportional(hr: Long, min: Long): String {
        val hourString = resourceRepo.getString(R.string.time_hour)
        val minutesProportion = min / 60f
        val proportional = hr + minutesProportion
        val proportionalString = "%.2f".format(proportional)

        return "$proportionalString$hourString"
    }

    fun toDayDateTitle(
        daysFromToday: Int,
        startOfDayShift: Long,
    ): String = synchronized(lock) {
        val calendar = Calendar.getInstance()

        calendar.apply {
            timeInMillis = currentTimestampProvider.get() - startOfDayShift
            add(Calendar.DATE, daysFromToday)
        }

        return dayTitleFormat.format(calendar.timeInMillis)
    }

    fun toDayShortDateTitle(
        daysFromToday: Int,
        startOfDayShift: Long,
    ): String = synchronized(lock) {
        val calendar = Calendar.getInstance()

        calendar.apply {
            timeInMillis = currentTimestampProvider.get() - startOfDayShift
            add(Calendar.DATE, daysFromToday)
        }

        return weekTitleFormat.format(calendar.timeInMillis)
    }

    fun toWeekDateTitle(
        weeksFromToday: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): String = synchronized(lock) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = toCalendarDayOfWeek(firstDayOfWeek)

        calendar.apply {
            this.firstDayOfWeek = dayOfWeek
            timeInMillis = currentTimestampProvider.get() - startOfDayShift
            setWeekToFirstDay()
            add(Calendar.DATE, weeksFromToday * 7)
        }
        val rangeStart = calendar.timeInMillis
        val rangeEnd = calendar.apply { add(Calendar.DATE, 6) }.timeInMillis

        return weekTitleFormat.format(rangeStart) + " - " + weekTitleFormat.format(rangeEnd)
    }

    fun toMonthDateTitle(
        monthsFromToday: Int,
        startOfDayShift: Long,
    ): String = synchronized(lock) {
        val calendar = Calendar.getInstance()

        calendar.apply {
            timeInMillis = currentTimestampProvider.get()
            shift(-startOfDayShift)
            add(Calendar.MONTH, monthsFromToday)
        }

        return monthTitleFormat.format(calendar.timeInMillis)
    }

    fun toYearDateTitle(
        yearsFromToday: Int,
        startOfDayShift: Long,
    ): String = synchronized(lock) {
        val calendar = Calendar.getInstance()

        calendar.apply {
            timeInMillis = currentTimestampProvider.get() - startOfDayShift
            add(Calendar.YEAR, yearsFromToday)
        }

        return yearTitleFormat.format(calendar.timeInMillis)
    }

    fun getStartOfDayTimeStamp(
        timestamp: Long = System.currentTimeMillis(),
        calendar: Calendar = Calendar.getInstance(),
    ): Long {
        return calendar.apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getDayOfWeek(
        timestamp: Long,
        calendar: Calendar,
        startOfDayShift: Long,
    ): DayOfWeek {
        return calendar
            .apply {
                timeInMillis = timestamp
                shift(-startOfDayShift)
            }
            .get(Calendar.DAY_OF_WEEK)
            .let(::toDayOfWeek)
    }

    private fun isFirstWeekOfNextYear(calendar: Calendar): Boolean {
        return calendar.get(Calendar.WEEK_OF_YEAR) == 1 &&
            calendar.get(Calendar.MONTH) == calendar.getActualMaximum(Calendar.MONTH)
    }

    companion object {
        private const val MINUTES_IN_MILLIS = 60_000
    }
}