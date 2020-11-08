package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataRange
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.DailyChartGrouping
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class StatisticsDetailInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val timeMapper: TimeMapper
) {

    suspend fun getDurations(
        typeId: Long,
        grouping: ChartGrouping,
        chartLength: ChartLength
    ): List<ChartBarDataDuration> {
        val numberOfGroups = getNumberOfGroups(chartLength)

        val ranges: List<ChartBarDataRange> = when (grouping) {
            ChartGrouping.DAILY -> getDailyGrouping(numberOfGroups)
            ChartGrouping.WEEKLY -> getWeeklyGrouping(numberOfGroups)
            ChartGrouping.MONTHLY -> getMonthlyGrouping(numberOfGroups)
        }

        val records = recordInteractor.getFromRange(
            start = ranges.first().rangeStart,
            end = ranges.last().rangeEnd
        ).filter { it.typeId == typeId }

        if (records.isEmpty()) return ranges.map {
            ChartBarDataDuration(
                legend = it.legend,
                duration = 0L
            )
        }

        return ranges
            .map { data ->
                val duration = getRecordsFromRange(records, data.rangeStart, data.rangeEnd)
                    .map { record -> clampToRange(record, data.rangeStart, data.rangeEnd) }
                    .let(::mapToDuration)

                ChartBarDataDuration(
                    legend = data.legend,
                    duration = duration
                )
            }
    }

    suspend fun getDailyDurations(typeId: Long): Map<DailyChartGrouping, Long> {
        val calendar = Calendar.getInstance()
        val dataDurations: MutableMap<DailyChartGrouping, Long> = mutableMapOf()
        val dataTimesTracked: MutableMap<DailyChartGrouping, Long> = mutableMapOf()

        val records = recordInteractor.getByType(listOf(typeId))
        val totalTracked = records.map { it.timeEnded - it.timeStarted }.sum()

        processRecords(calendar, records).forEach {
            val day = mapToDailyGrouping(calendar, it)
            val duration = it.timeEnded - it.timeStarted
            dataDurations[day] = dataDurations[day].orZero() + duration
            dataTimesTracked[day] = dataTimesTracked[day].orZero() + 1
        }

        val daysTracked = dataTimesTracked.values.filter { it != 0L }.size

        return dataDurations.mapValues { (_, duration) ->
            when {
                totalTracked != 0L -> duration * 100 / totalTracked
                daysTracked != 0 -> 100L / daysTracked
                else -> 100L
            }
        }
    }

    private fun processRecords(calendar: Calendar, records: List<Record>): List<Record> {
        val processedRecords: MutableList<Record> = mutableListOf()

        records.forEach { record ->
            splitIntoRecords(calendar, record).forEach { processedRecords.add(it) }
        }

        return processedRecords
    }

    private fun mapToDailyGrouping(calendar: Calendar, record: Record): DailyChartGrouping {
        val day = calendar
            .apply { timeInMillis = record.timeStarted }
            .get(Calendar.DAY_OF_WEEK)

        return when (day) {
            Calendar.MONDAY -> DailyChartGrouping.MONDAY
            Calendar.TUESDAY -> DailyChartGrouping.TUESDAY
            Calendar.WEDNESDAY -> DailyChartGrouping.WEDNESDAY
            Calendar.THURSDAY -> DailyChartGrouping.THURSDAY
            Calendar.FRIDAY -> DailyChartGrouping.FRIDAY
            Calendar.SATURDAY -> DailyChartGrouping.SATURDAY
            else -> DailyChartGrouping.SUNDAY
        }
    }

    private tailrec fun splitIntoRecords(
        calendar: Calendar,
        record: Record,
        splitRecords: MutableList<Record> = mutableListOf()
    ): List<Record> {
        if (timeMapper.sameDay(record.timeStarted, record.timeEnded)) return splitRecords.also { it.add(record) }

        val adjustedCalendar = calendar.apply {
            timeInMillis = record.timeStarted
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val rangeEnd = adjustedCalendar.apply { add(Calendar.DATE, 1) }.timeInMillis

        val firstRecord = record.copy(
            timeStarted = record.timeStarted,
            timeEnded = rangeEnd
        )
        val secondRecord = record.copy(
            timeStarted = rangeEnd,
            timeEnded = record.timeEnded
        )
        splitRecords.add(firstRecord)

        return splitIntoRecords(calendar, secondRecord, splitRecords)
    }

    private fun getNumberOfGroups(
        chartLength: ChartLength
    ): Int {
        return when (chartLength) {
            ChartLength.TEN -> 10
            ChartLength.FIFTY -> 50
            ChartLength.HUNDRED -> 100
        }
    }

    private fun getDailyGrouping(
        numberOfDays: Int
    ): List<ChartBarDataRange> {
        val calendar = Calendar.getInstance()

        return (numberOfDays - 1 downTo 0).map { shift ->
            calendar.apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.add(Calendar.DATE, -shift)

            val legend = if (numberOfDays <= 10) {
                timeMapper.formatShortDay(calendar.timeInMillis)
            } else {
                ""
            }
            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis

            ChartBarDataRange(
                legend = legend,
                rangeStart = rangeStart,
                rangeEnd = rangeEnd
            )
        }
    }

    private fun getWeeklyGrouping(
        numberOfWeeks: Int
    ): List<ChartBarDataRange> {
        val calendar = Calendar.getInstance()

        return (numberOfWeeks - 1 downTo 0).map { shift ->
            calendar.apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.add(Calendar.DATE, -shift * 7)

            val legend = if (numberOfWeeks <= 10) {
                timeMapper.formatShortMonth(calendar.timeInMillis)
            } else {
                ""
            }
            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.DATE, 7) }.timeInMillis

            ChartBarDataRange(
                legend = legend,
                rangeStart = rangeStart,
                rangeEnd = rangeEnd
            )
        }
    }

    private fun getMonthlyGrouping(
        numberOfMonths: Int
    ): List<ChartBarDataRange> {
        val calendar = Calendar.getInstance()

        return (numberOfMonths - 1 downTo 0).map { shift ->
            calendar.apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, -shift)

            val legend = if (numberOfMonths <= 10) {
                timeMapper.formatShortMonth(calendar.timeInMillis)
            } else {
                ""
            }
            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis

            ChartBarDataRange(
                legend = legend,
                rangeStart = rangeStart,
                rangeEnd = rangeEnd
            )
        }
    }

    private fun getRecordsFromRange(
        records: List<Record>,
        rangeStart: Long,
        rangeEnd: Long
    ): List<Record> {
        return records.filter { it.timeStarted < rangeEnd && it.timeEnded > rangeStart }
    }

    private fun clampToRange(
        record: Record,
        rangeStart: Long,
        rangeEnd: Long
    ): Pair<Long, Long> {
        return max(record.timeStarted, rangeStart) to min(record.timeEnded, rangeEnd)
    }

    private fun mapToDuration(ranges: List<Pair<Long, Long>>): Long {
        return ranges
            .map { it.second - it.first }
            .sum()
    }
}