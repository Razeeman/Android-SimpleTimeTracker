package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.common.R
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.core.extension.shift
import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.padDuration
import com.example.util.simpletimetracker.domain.extension.rotateLeft
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

class TimeMapper @Inject constructor(
    localeProvider: LocaleProvider,
    private val resourceRepo: BaseResourceRepo,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    private val locale: Locale by lazy { localeProvider.get() }

    private val timeFormat by lazy { SimpleDateFormat("h:mm a", locale) }
    private val timeFormatWithSeconds by lazy { SimpleDateFormat("h:mm:ss a", locale) }
    private val timeFormatMilitary by lazy { SimpleDateFormat("HH:mm", locale) }
    private val timeFormatMilitaryWithSeconds by lazy { SimpleDateFormat("HH:mm:ss", locale) }

    private val dateFormat by lazy { SimpleDateFormat("MMM d", locale) }

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

    // Mar 11
    fun formatDate(
        time: Long,
    ): String = synchronized(lock) {
        return dateFormat.format(time)
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

    fun toTimestampShifted(
        rangesFromToday: Int,
        range: RangeLength,
        startOfDayShift: Long = 0,
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

        return Calendar.getInstance()
            .apply {
                timeInMillis = System.currentTimeMillis()
                shift(-startOfDayShift)
                add(calendarStep, rangesFromToday)
            }
            .timeInMillis
    }

    fun toTimestampShift(
        fromTime: Long = System.currentTimeMillis(),
        toTime: Long,
        range: RangeLength,
        firstDayOfWeek: DayOfWeek,
        calendar: Calendar = Calendar.getInstance(),
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
     *
     * ex. 1h, 1h 1m, 1h 1m 1s, 1h 1s, 1m 1s, 1s.
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
            if (hr != 0L) it.padDuration() else it
        } + minuteString
        val secString = sec.toString().let {
            if (hr != 0L || min != 0L) it.padDuration() else it
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
                val daysBetween = toTimestampShift(
                    fromTime = rangeLength.range.timeStarted,
                    toTime = rangeLength.range.timeEnded,
                    range = RangeLength.Day,
                    firstDayOfWeek = firstDayOfWeek,
                ).toInt()
                calendar.timeInMillis = rangeLength.range.timeStarted
                calendar.add(Calendar.DATE, shift * daysBetween)
                rangeStart = calendar.timeInMillis
                calendar.timeInMillis = rangeLength.range.timeEnded
                calendar.add(Calendar.DATE, shift * daysBetween)
                rangeEnd = calendar.timeInMillis
            }

            is RangeLength.Last -> {
                calendar.add(Calendar.DATE, shift * rangeLength.days)
                rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis
                rangeStart = calendar.apply { add(Calendar.DATE, -rangeLength.days) }.timeInMillis
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
     * @param durationFormat - PROPORTIONAL_MINUTES 1.25h
     *
     * ex. 1d 0h 0m, 1h 0m, 1s.
     */
    fun formatInterval(
        interval: Long,
        forceSeconds: Boolean,
        durationFormat: DurationFormat,
    ): String {
        val dayString = resourceRepo.getString(R.string.time_day)
        val hourString = resourceRepo.getString(R.string.time_hour)
        val minuteString = resourceRepo.getString(R.string.time_minute)
        val secondString = resourceRepo.getString(R.string.time_second)

        var intervalLeft = abs(interval)
        val days: Long = if (durationFormat == DurationFormat.DAYS) {
            TimeUnit.MILLISECONDS.toDays(intervalLeft)
        } else {
            0
        }
        intervalLeft -= TimeUnit.DAYS.toMillis(days)
        val hr: Long = if (durationFormat != DurationFormat.MINUTES) {
            TimeUnit.MILLISECONDS.toHours(intervalLeft)
        } else {
            0
        }
        intervalLeft -= TimeUnit.HOURS.toMillis(hr)
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(intervalLeft)
        intervalLeft -= TimeUnit.MINUTES.toMillis(min)
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(intervalLeft)

        val result = when (durationFormat) {
            DurationFormat.PROPORTIONAL_MINUTES -> {
                formatIntervalProportional(hr, min)
            }
            else -> {
                val willShowDays = durationFormat == DurationFormat.DAYS && days != 0L
                val willShowHours = willShowDays || hr != 0L

                val willShowMinutes: Boolean
                val willShowSeconds: Boolean

                if (forceSeconds) {
                    willShowMinutes = willShowHours || min != 0L
                    willShowSeconds = true
                } else {
                    willShowMinutes = true
                    willShowSeconds = false
                }

                var res = ""
                if (willShowDays) res += "$days$dayString"
                if (willShowHours) {
                    if (res.isNotEmpty()) res += " "
                    res += "$hr$hourString"
                }
                if (willShowMinutes) {
                    if (res.isNotEmpty()) res += " "
                    res += "$min$minuteString"
                }
                if (willShowSeconds) {
                    if (res.isNotEmpty()) res += " "
                    res += "$sec$secondString"
                }
                res
            }
        }

        return if (interval < 0) "-$result" else result
    }

    private fun formatIntervalProportional(hr: Long, min: Long): String {
        val hourString = resourceRepo.getString(R.string.time_hour)
        val minutesProportion = min / 60f
        val proportional = hr + minutesProportion
        val proportionalString = "%.2f".format(proportional)

        return "$proportionalString$hourString"
    }

    fun toDayDateTimestamp(
        daysFromToday: Int,
        startOfDayShift: Long,
    ): Long {
        val calendar = Calendar.getInstance()

        calendar.apply {
            // Shifted by startOfDayShift, so that for example:
            // now it is 01:00 17.10.2025 but today starts at 2:00,
            // it would show 16.10.2025.
            timeInMillis = currentTimestampProvider.get()
            shift(-startOfDayShift)
            add(Calendar.DATE, daysFromToday)
        }

        return calendar.timeInMillis
    }

    fun toDayDateTitle(
        daysFromToday: Int,
        startOfDayShift: Long,
    ): String = synchronized(lock) {
        val calendar = toDayDateTimestamp(daysFromToday, startOfDayShift)
        return dayTitleFormat.format(calendar)
    }

    fun toDayShortDateTitle(
        daysFromToday: Int,
        startOfDayShift: Long,
    ): String = synchronized(lock) {
        val calendar = toDayDateTimestamp(daysFromToday, startOfDayShift)
        return weekTitleFormat.format(calendar)
    }

    fun toWeekDateTimestamp(
        weeksFromToday: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val dayOfWeek = toCalendarDayOfWeek(firstDayOfWeek)

        calendar.apply {
            this.firstDayOfWeek = dayOfWeek
            timeInMillis = currentTimestampProvider.get()
            shift(-startOfDayShift)
            setWeekToFirstDay()
            add(Calendar.DATE, weeksFromToday * 7)
        }
        val rangeStart = calendar.timeInMillis
        val rangeEnd = calendar.apply { add(Calendar.DATE, 6) }.timeInMillis

        return rangeStart to rangeEnd
    }

    fun toWeekDateTitle(
        weeksFromToday: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): String = synchronized(lock) {
        val (rangeStart, rangeEnd) = toWeekDateTimestamp(weeksFromToday, startOfDayShift, firstDayOfWeek)
        return weekTitleFormat.format(rangeStart) + " - " + weekTitleFormat.format(rangeEnd)
    }

    fun toMonthDateTimestamp(
        monthsFromToday: Int,
        startOfDayShift: Long,
    ): Long {
        val calendar = Calendar.getInstance()

        return calendar.apply {
            timeInMillis = currentTimestampProvider.get()
            shift(-startOfDayShift)
            add(Calendar.MONTH, monthsFromToday)
        }.timeInMillis
    }

    fun toMonthDateTitle(
        monthsFromToday: Int,
        startOfDayShift: Long,
    ): String = synchronized(lock) {
        val calendar = toMonthDateTimestamp(monthsFromToday, startOfDayShift)
        return monthTitleFormat.format(calendar)
    }

    fun toYearDateTimestamp(
        yearsFromToday: Int,
        startOfDayShift: Long,
    ): Long {
        val calendar = Calendar.getInstance()

        return calendar.apply {
            timeInMillis = currentTimestampProvider.get()
            shift(-startOfDayShift)
            add(Calendar.YEAR, yearsFromToday)
        }.timeInMillis
    }

    fun toYearDateTitle(
        yearsFromToday: Int,
        startOfDayShift: Long,
    ): String = synchronized(lock) {
        val calendar = toYearDateTimestamp(yearsFromToday, startOfDayShift)
        return yearTitleFormat.format(calendar)
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

    fun mapFromStartOfDay(
        timeStamp: Long,
        calendar: Calendar,
    ): Long {
        return calendar.apply {
            timeInMillis = timeStamp
            setToStartOfDay()
        }.let {
            timeStamp - it.timeInMillis
        }
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

    fun getWeekOrder(
        firstDayOfWeek: DayOfWeek,
    ): List<DayOfWeek> {
        return listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        ).let { list ->
            list.indexOf(firstDayOfWeek)
                .takeUnless { it == -1 }.orZero()
                .let(list::rotateLeft)
        }
    }

    fun getFormattedDateTime(
        time: Long,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
    ): DateTime {
        return DateTime(
            date = formatDate(
                time = time,
            ),
            time = formatTime(
                time = time,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
        )
    }

    fun formatDays(
        firstDayOfWeek: DayOfWeek,
        selectedDaysOfWeek: Set<DayOfWeek>,
    ): String {
        if (selectedDaysOfWeek.containsAll(DayOfWeek.entries)) return ""

        val daysInOrder = getWeekOrder(firstDayOfWeek).map { dayOfWeek ->
            dayOfWeek to toShortDayOfWeekName(dayOfWeek)
        }

        val runs = mutableListOf<MutableList<Pair<DayOfWeek, String>>>()

        daysInOrder.forEach { day ->
            if (day.first in selectedDaysOfWeek) {
                runs.lastOrNull()?.add(day) ?: runs.add(mutableListOf(day))
            } else if (runs.lastOrNull()?.isNotEmpty() == true) {
                runs.add(mutableListOf())
            }
        }

        return runs
            .filter { it.isNotEmpty() }
            .joinToString(separator = " ") { run ->
                when (run.size) {
                    1 -> run.first().second
                    else -> "${run.first().second}-${run.last().second}"
                }
            }
    }

    private fun isFirstWeekOfNextYear(calendar: Calendar): Boolean {
        return calendar.get(Calendar.WEEK_OF_YEAR) == 1 &&
            calendar.get(Calendar.MONTH) == calendar.getActualMaximum(Calendar.MONTH)
    }

    data class DateTime(
        val date: String,
        val time: String,
    )
}