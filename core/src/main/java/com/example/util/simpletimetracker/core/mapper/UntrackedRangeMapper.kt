package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.record.model.Range
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class UntrackedRangeMapper @Inject constructor(
    private val timeMapper: TimeMapper,
) {

    fun processSettings(
        ranges: List<Range>,
        daysOfWeek: Set<DayOfWeek>,
        timeOfDay: Range?,
    ): List<Range> {
        val calendar = Calendar.getInstance()
        return processTimeOfDay(
            ranges = ranges,
            timeOfDay = timeOfDay,
            calendar = calendar,
        ).let {
            processDaysOfWeek(
                ranges = it,
                daysOfWeek = daysOfWeek,
                calendar = calendar,
            )
        }
    }

    private fun processTimeOfDay(
        ranges: List<Range>,
        timeOfDay: Range?,
        calendar: Calendar,
    ): List<Range> {
        if (timeOfDay == null || timeOfDay.duration == 0L) return ranges

        return ranges.flatMap {
            processTimeOfDayRecord(
                record = it,
                timeOfDay = timeOfDay,
                calendar = calendar,
            )
        }
    }

    private fun processDaysOfWeek(
        ranges: List<Range>,
        daysOfWeek: Set<DayOfWeek>,
        calendar: Calendar,
    ): List<Range> {
        if (daysOfWeek.isEmpty()) return emptyList()
        if (daysOfWeek.containsAll(DayOfWeek.entries)) return ranges

        return ranges.flatMap { range ->
            processDaysOfWeekRange(
                range = range,
                daysOfWeek = daysOfWeek,
                calendar = calendar,
            )
        }
    }

    private fun processTimeOfDayRecord(
        record: Range,
        timeOfDay: Range,
        calendar: Calendar,
    ): List<Range> {
        val result = mutableListOf<Range>()

        val recordStartOfDay = timeMapper.getStartOfDayTimeStamp(record.timeStarted, calendar)
        var currentTimeOfDayRangeStart = if (timeOfDay.timeStarted < timeOfDay.timeEnded) {
            getTimeOfDayTimestamp(recordStartOfDay, timeOfDay.timeStarted, calendar)
        } else {
            val prevStartOfDay = recordStartOfDay.addDay(-1, calendar)
            getTimeOfDayTimestamp(prevStartOfDay, timeOfDay.timeStarted, calendar)
        }
        var currentTimeOfDayRangeEnd = getTimeOfDayTimestamp(recordStartOfDay, timeOfDay.timeEnded, calendar)

        result += intersect(
            first = record,
            second = Range(currentTimeOfDayRangeStart, currentTimeOfDayRangeEnd),
        )

        while (currentTimeOfDayRangeEnd < record.timeEnded) {
            currentTimeOfDayRangeStart = currentTimeOfDayRangeStart.addDay(1, calendar)
            currentTimeOfDayRangeEnd = currentTimeOfDayRangeEnd.addDay(1, calendar)
            result += intersect(
                first = record,
                second = Range(currentTimeOfDayRangeStart, currentTimeOfDayRangeEnd),
            )
        }

        return result
    }

    private fun processDaysOfWeekRange(
        range: Range,
        daysOfWeek: Set<DayOfWeek>,
        calendar: Calendar,
    ): List<Range> {
        val result = mutableListOf<Range>()

        var dayStart = timeMapper.getStartOfDayTimeStamp(range.timeStarted, calendar)

        while (dayStart < range.timeEnded) {
            val nextDayStart = dayStart.addDay(1, calendar)
            val dayOfWeek = timeMapper.getDayOfWeek(
                timestamp = dayStart,
                calendar = calendar,
                startOfDayShift = 0, // TODO support start of day?
            )

            if (dayOfWeek in daysOfWeek) {
                val eligibleRange = intersect(
                    first = range,
                    second = Range(dayStart, nextDayStart),
                )
                val lastRange = result.lastOrNull()

                if (eligibleRange != null && lastRange?.timeEnded == eligibleRange.timeStarted) {
                    // Rejoin continuous ranges split at midnight by day-by-day filtering.
                    // E.g. Mon 17:00–Tue 00:00 + Tue 00:00–08:00 becomes Mon 17:00–Tue 08:00.
                    result[result.lastIndex] = Range(
                        timeStarted = lastRange.timeStarted,
                        timeEnded = eligibleRange.timeEnded,
                    )
                } else {
                    result += eligibleRange
                }
            }

            dayStart = nextDayStart
        }

        return result
    }

    private fun getTimeOfDayTimestamp(
        dayStart: Long,
        timeOfDay: Long,
        calendar: Calendar,
    ): Long {
        val hours = (timeOfDay / MILLIS_IN_HOUR).toInt()
        val minutes = (timeOfDay % MILLIS_IN_HOUR / MILLIS_IN_MINUTE).toInt()
        val seconds = (timeOfDay % MILLIS_IN_MINUTE / MILLIS_IN_SECOND).toInt()
        val millis = (timeOfDay % MILLIS_IN_SECOND).toInt()

        return calendar.apply {
            timeInMillis = dayStart
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, seconds)
            set(Calendar.MILLISECOND, millis)
        }.timeInMillis
    }

    private fun intersect(first: Range, second: Range): Range? {
        val start = max(first.timeStarted, second.timeStarted)
        val end = min(first.timeEnded, second.timeEnded)

        return if (start < end) Range(start, end) else null
    }

    private fun Long.addDay(count: Int, calendar: Calendar): Long {
        calendar.timeInMillis = this
        calendar.add(Calendar.DATE, count)
        return calendar.timeInMillis
    }

    private companion object {
        const val MILLIS_IN_SECOND = 1_000L
        const val MILLIS_IN_MINUTE = 60 * MILLIS_IN_SECOND
        const val MILLIS_IN_HOUR = 60 * MILLIS_IN_MINUTE
    }
}
