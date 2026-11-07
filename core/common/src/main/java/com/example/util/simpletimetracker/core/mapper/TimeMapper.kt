package com.example.util.simpletimetracker.core.mapper

import android.annotation.SuppressLint
import android.icu.text.DateFormatSymbols
import android.icu.text.DateTimePatternGenerator
import android.icu.util.ULocale
import com.example.util.simpletimetracker.core.common.R
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.core.extension.shift
import com.example.util.simpletimetracker.core.model.CalendarDate
import com.example.util.simpletimetracker.core.model.CalendarTime
import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.provider.TimeZoneProvider
import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.calendar.CalendarTypeProvider
import com.example.util.simpletimetracker.domain.calendar.model.CalendarType
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.padDuration
import com.example.util.simpletimetracker.domain.extension.rotateLeft
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import android.icu.text.SimpleDateFormat as IcuSimpleDateFormat
import android.icu.util.Calendar as IcuCalendar
import android.icu.util.TimeZone as IcuTimeZone

class TimeMapper @Inject constructor(
    localeProvider: LocaleProvider,
    private val resourceRepo: BaseResourceRepo,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val calendarTypeProvider: CalendarTypeProvider,
    private val timeZoneProvider: TimeZoneProvider,
) {

    private val locale: Locale by lazy { localeProvider.get() }

    private val timeFormat = "h:mm a"
    private val timeFormatWithSeconds = "h:mm:ss a"
    private val timeFormatMilitary = "HH:mm"
    private val timeFormatMilitaryWithSeconds = "HH:mm:ss"

    private val dateFormat = "MMM d"

    private val dateTimeFormat = "MMM d h:mm a"
    private val dateTimeFormatWithSeconds = "MMM d h:mm:ss a"
    private val dateTimeFormatMilitary = "MMM d HH:mm"
    private val dateTimeFormatMilitaryWithSeconds = "MMM d HH:mm:ss"

    private val dateTimeYearFormat = "MMM d yyyy h:mm a"
    private val dateTimeYearFormatMilitary = "MMM d yyyy HH:mm"

    private val dayDateYearFormat = "E, MMM d yyyy"
    private val dateYearFormat = "MMM d yyyy"
    private val shortDayFormatMMDD = "MM.dd"
    private val shortDayFormatDDMM = "dd.MM"
    private val shortMonthFormat = "MMM"
    private val shortYearFormat = "yy"

    private val dayTitleFormat = "E, MMM d"
    private val weekTitleFormat = "MMM d"
    private val monthTitleFormat = "MMMM"
    private val yearTitleFormat = "yyyy"

    private val dayDatePattern by lazy {
        DateTimePatternGenerator.getInstance(calendarLocale())
            .getBestPattern("EMMMd")
    }
    private val monthYearPattern by lazy {
        DateTimePatternGenerator.getInstance(calendarLocale())
            .getBestPattern("yMMMM")
    }
    private val narrowDayOfWeekNames by lazy {
        DateFormatSymbols(calendarLocale())
            .getWeekdays(DateFormatSymbols.FORMAT, DateFormatSymbols.NARROW)
    }

    // Locale and calendar selection changes restart the app, so only the pattern varies.
    private val calendarFormats = mutableMapOf<String, IcuSimpleDateFormat>()

    private val lock = Any()

    fun formatDayOfMonth(
        timestamp: Long,
        padded: Boolean,
    ): String {
        return formatCalendarField(timestamp, if (padded) "dd" else "d")
    }

    fun formatYear(
        timestamp: Long,
    ): String {
        return formatCalendarField(timestamp, yearTitleFormat)
    }

    private fun calendarLocale(): ULocale {
        val keyword = when (calendarTypeProvider.get()) {
            CalendarType.GREGORIAN -> "gregorian"
            CalendarType.PERSIAN -> "persian"
        }
        return ULocale.forLocale(locale).setKeywordValue("calendar", keyword)
    }

    fun newCalendar(
        timeZone: TimeZone = timeZoneProvider.get(),
    ): IcuCalendar {
        return IcuCalendar.getInstance(
            IcuTimeZone.getTimeZone(timeZone.id),
            calendarLocale(),
        )
    }

    fun toCalendarDate(
        timestamp: Long,
    ): CalendarDate {
        return newCalendar().apply { timeInMillis = timestamp }
            .toCalendarDate()
    }

    private fun IcuCalendar.toCalendarDate(): CalendarDate {
        return CalendarDate(
            year = get(IcuCalendar.YEAR),
            month = get(IcuCalendar.MONTH) + 1,
            day = get(IcuCalendar.DAY_OF_MONTH),
        )
    }

    fun toTimestamp(
        date: CalendarDate,
    ): Long {
        return newCalendar().apply {
            clear()
            set(date.year, date.month - 1, date.day)
        }.timeInMillis
    }

    fun replaceDate(
        timestamp: Long,
        date: CalendarDate,
    ): Long {
        return newCalendar().apply {
            timeInMillis = timestamp
            set(IcuCalendar.DAY_OF_MONTH, 1)
            set(date.year, date.month - 1, date.day)
        }.timeInMillis
    }

    fun replaceTime(
        timestamp: Long,
        time: CalendarTime,
    ): Long {
        return newCalendar().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, time.second)
        }.timeInMillis
    }

    /** Returns a half-open reporting period containing [timestamp]. */
    fun getPeriodRange(
        timestamp: Long,
        rangeLength: RangeLength,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        shift: Int = 0,
    ): Range {
        require(
            rangeLength is RangeLength.Day || rangeLength is RangeLength.Week ||
                rangeLength is RangeLength.Month || rangeLength is RangeLength.Year,
        ) { "Unsupported reporting period: $rangeLength" }
        val timeZone: TimeZone = timeZoneProvider.get()
        val anchor = Calendar.getInstance(timeZone, Locale.ROOT).apply {
            timeInMillis = timestamp
            shift(-startOfDayShift)
        }
        if (rangeLength is RangeLength.Day || rangeLength is RangeLength.Week) {
            val days = if (rangeLength is RangeLength.Week) 7 else 1
            val weekStart = toCalendarDayOfWeek(firstDayOfWeek)
            anchor.apply {
                this.firstDayOfWeek = weekStart
                if (rangeLength is RangeLength.Week) setWeekToFirstDay()
                setToStartOfDay()
            }
            val start = anchor.timeInMillis
            val end = anchor.apply { add(Calendar.DATE, days) }.timeInMillis
            return shiftPeriodRange(start, end, startOfDayShift, timeZone)
        }
        val date = toCalendarDate(anchor.timeInMillis)
        // Work on civil fields independently of DST, then resolve each boundary locally.
        val nominal = newCalendar(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(date.year, date.month - 1, date.day)
        }
        val field = when (rangeLength) {
            is RangeLength.Month -> {
                nominal.set(IcuCalendar.DAY_OF_MONTH, 1)
                IcuCalendar.MONTH
            }
            is RangeLength.Year -> {
                nominal.set(IcuCalendar.DAY_OF_MONTH, 1)
                nominal.set(IcuCalendar.MONTH, 0)
                IcuCalendar.YEAR
            }
            else -> error("Unsupported reporting period: $rangeLength")
        }
        nominal.add(field, shift)
        val start = toTimestamp(nominal.toCalendarDate())
        nominal.add(field, 1)
        val end = toTimestamp(nominal.toCalendarDate())
        return shiftPeriodRange(start, end, startOfDayShift, timeZone)
    }

    private fun shiftPeriodRange(
        start: Long,
        end: Long,
        startOfDayShift: Long,
        timeZone: TimeZone,
    ): Range {
        // Resolve each boundary's own DST offset, including the rollback for a gap.
        return Range(
            Calendar.getInstance(timeZone, Locale.ROOT).apply { timeInMillis = start }
                .shift(startOfDayShift).timeInMillis,
            Calendar.getInstance(timeZone, Locale.ROOT).apply { timeInMillis = end }
                .shift(startOfDayShift).timeInMillis,
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatCalendarField(
        timestamp: Long,
        pattern: String,
    ): String = synchronized(lock) {
        val timeZone: TimeZone = timeZoneProvider.get()
        return calendarFormats.getOrPut(pattern) {
            IcuSimpleDateFormat(pattern, calendarLocale()).apply {
                calendar = newCalendar(timeZone)
            }
        }.apply {
            if (this.timeZone.id != timeZone.id) {
                this.timeZone = IcuTimeZone.getTimeZone(timeZone.id)
            }
        }.format(java.util.Date(timestamp))
    }

    // 12:21
    fun formatTime(
        time: Long,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
    ): String {
        val pattern = if (useMilitaryTime) {
            if (showSeconds) timeFormatMilitaryWithSeconds else timeFormatMilitary
        } else {
            if (showSeconds) timeFormatWithSeconds else timeFormat
        }
        return formatCalendarField(time, pattern)
    }

    // Mar 11
    fun formatDate(
        time: Long,
    ): String {
        return formatCalendarField(time, dateFormat)
    }

    // Mar 11 12:21
    fun formatDateTime(
        time: Long,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
    ): String {
        val pattern = if (useMilitaryTime) {
            if (showSeconds) dateTimeFormatMilitaryWithSeconds else dateTimeFormatMilitary
        } else {
            if (showSeconds) dateTimeFormatWithSeconds else dateTimeFormat
        }
        return formatCalendarField(time, pattern)
    }

    // Mar 12 2021 12:21
    fun formatDateTimeYear(time: Long, useMilitaryTime: Boolean): String {
        val pattern = if (useMilitaryTime) dateTimeYearFormatMilitary else dateTimeYearFormat
        return formatCalendarField(time, pattern)
    }

    // Mar 12 2021
    fun formatDateYear(time: Long): String {
        return formatCalendarField(time, dateYearFormat)
    }

    // Tue, Mar 12 2021
    fun formatDayDateYear(time: Long): String {
        return formatCalendarField(time, dayDateYearFormat)
    }

    fun formatDayDate(time: Long): String {
        return formatCalendarField(time, dayDatePattern)
    }

    fun formatMonthYear(time: Long): String {
        return formatCalendarField(time, monthYearPattern)
    }

    // 12.03
    fun formatShortDay(time: Long, useMonthDayTimeFormat: Boolean): String {
        val pattern = if (useMonthDayTimeFormat) shortDayFormatMMDD else shortDayFormatDDMM
        return formatCalendarField(time, pattern)
    }

    // Mar
    fun formatShortMonth(time: Long): String {
        return formatCalendarField(time, shortMonthFormat)
    }

    // 21
    fun formatShortYear(time: Long): String {
        return formatCalendarField(time, shortYearFormat)
    }

    fun toTimestampShifted(
        rangesFromToday: Int,
        range: RangeLength,
        startOfDayShift: Long = 0,
    ): Long {
        val calendarStep = when (range) {
            is RangeLength.Day -> IcuCalendar.DAY_OF_YEAR
            is RangeLength.Week -> IcuCalendar.WEEK_OF_YEAR
            is RangeLength.Month -> IcuCalendar.MONTH
            is RangeLength.Year -> IcuCalendar.YEAR
            is RangeLength.All -> return 0
            is RangeLength.Custom -> return 0
            is RangeLength.Last -> return 0
        }
        return newCalendar()
            .apply {
                timeInMillis = currentTimestampProvider.get()
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
        calendar: Calendar = Calendar.getInstance(timeZoneProvider.get()),
    ): Long {
        val calendarStep = when (range) {
            is RangeLength.Day -> Calendar.DAY_OF_YEAR
            is RangeLength.Week -> Calendar.WEEK_OF_YEAR
            is RangeLength.Month,
            is RangeLength.Year,
            -> {
                // Month/year boundaries depend on the selected calendar (Gregorian or Persian).
                // Count period boundaries using its fields; both calendars have 12 months per year.
                val fromDate = toCalendarDate(fromTime)
                val toDate = toCalendarDate(toTime)
                val years = toDate.year.toLong() - fromDate.year
                return if (range is RangeLength.Month) {
                    years * 12 + toDate.month - fromDate.month
                } else {
                    years
                }
            }
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

        calendar.timeInMillis = fromTime
        result -= if (calendarStep == Calendar.WEEK_OF_YEAR && isFirstWeekOfNextYear(calendar)) {
            calendar.getActualMaximum(Calendar.WEEK_OF_YEAR) + 1
        } else {
            calendar.get(calendarStep)
        }
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

    fun toNarrowDayOfWeekName(dayOfWeek: DayOfWeek): String {
        return narrowDayOfWeekNames[toCalendarDayOfWeek(dayOfWeek)]
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

    /** Returns the requested reporting range relative to [referenceTimestamp], defaulting to the current instant. */
    fun getRangeStartAndEnd(
        rangeLength: RangeLength,
        shift: Int,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        referenceTimestamp: Long = currentTimestampProvider.get(),
    ): Range {
        when (rangeLength) {
            is RangeLength.Month,
            is RangeLength.Year,
            -> {
                return getPeriodRange(
                    timestamp = referenceTimestamp,
                    rangeLength = rangeLength,
                    firstDayOfWeek = firstDayOfWeek,
                    startOfDayShift = startOfDayShift,
                    shift = shift,
                )
            }
            else -> Unit
        }

        val dayOfWeek = toCalendarDayOfWeek(firstDayOfWeek)
        val rangeStart: Long
        val rangeEnd: Long
        val calendar = Calendar.getInstance(Locale.ROOT).apply {
            this.firstDayOfWeek = dayOfWeek
            timeInMillis = referenceTimestamp

            shift(-startOfDayShift)
            if (rangeLength is RangeLength.Week) setWeekToFirstDay()
            setToStartOfDay()
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

            is RangeLength.Month,
            is RangeLength.Year,
            -> {
                // Already handled.
                return Range(0, 0)
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

    /**
     * [firstDayOfWeek] can affect week fields such as WEEK_OF_MONTH and WEEK_OF_YEAR,
     * though current callers only request day fields.
     */
    fun getActualMaximum(
        startDate: Long,
        field: Int,
        firstDayOfWeek: DayOfWeek,
    ): Int {
        val dayOfWeek = toCalendarDayOfWeek(firstDayOfWeek)
        return newCalendar()
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
        val calendar = Calendar.getInstance(timeZoneProvider.get())

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
    ): String {
        val calendar = toDayDateTimestamp(daysFromToday, startOfDayShift)
        return formatCalendarField(calendar, dayTitleFormat)
    }

    fun toDayShortDateTitle(
        daysFromToday: Int,
        startOfDayShift: Long,
    ): String {
        val calendar = toDayDateTimestamp(daysFromToday, startOfDayShift)
        return formatCalendarField(calendar, weekTitleFormat)
    }

    fun toWeekDateTimestamp(
        weeksFromToday: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): Pair<Long, Long> {
        val calendar = Calendar.getInstance(timeZoneProvider.get())
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
    ): String {
        val (rangeStart, rangeEnd) = toWeekDateTimestamp(weeksFromToday, startOfDayShift, firstDayOfWeek)
        return formatCalendarField(rangeStart, weekTitleFormat) +
            " - " + formatCalendarField(rangeEnd, weekTitleFormat)
    }

    fun toMonthDateTimestamp(
        monthsFromToday: Int,
        startOfDayShift: Long,
    ): Long {
        return toCalendarDateTimestamp(
            amount = monthsFromToday,
            startOfDayShift = startOfDayShift,
            field = IcuCalendar.MONTH,
        )
    }

    fun toMonthDateTitle(
        monthsFromToday: Int,
        startOfDayShift: Long,
    ): String {
        val calendar = toMonthDateTimestamp(monthsFromToday, startOfDayShift)
        return formatCalendarField(calendar, monthTitleFormat)
    }

    fun toYearDateTimestamp(
        yearsFromToday: Int,
        startOfDayShift: Long,
    ): Long {
        return toCalendarDateTimestamp(
            amount = yearsFromToday,
            startOfDayShift = startOfDayShift,
            field = IcuCalendar.YEAR,
        )
    }

    fun toYearDateTitle(
        yearsFromToday: Int,
        startOfDayShift: Long,
    ): String {
        val calendar = toYearDateTimestamp(yearsFromToday, startOfDayShift)
        return formatCalendarField(calendar, yearTitleFormat)
    }

    private fun toCalendarDateTimestamp(
        amount: Int,
        startOfDayShift: Long,
        field: Int,
    ): Long {
        return newCalendar().apply {
            timeInMillis = currentTimestampProvider.get()
            shift(-startOfDayShift)
            add(field, amount)
        }.timeInMillis
    }

    fun getStartOfDayTimeStamp(
        timestamp: Long = System.currentTimeMillis(),
        calendar: Calendar = Calendar.getInstance(timeZoneProvider.get()),
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