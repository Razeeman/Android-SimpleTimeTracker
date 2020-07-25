package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.Record
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class StatisticsDetailInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor
) {

    suspend fun getDurations(
        typeId: Long,
        grouping: ChartGrouping,
        chartLength: ChartLength
    ): List<Long> {
        val numberOfGroups = getNumberOfGroups(chartLength)

        val ranges = when (grouping) {
            ChartGrouping.DAILY -> getDailyGrouping(numberOfGroups)
            ChartGrouping.WEEKLY -> getWeeklyGrouping(numberOfGroups)
            ChartGrouping.MONTHLY -> getMonthlyGrouping(numberOfGroups)
        }

        val records = recordInteractor.getFromRange(
            start = ranges.first().first,
            end = ranges.last().second
        ).filter { it.typeId == typeId }

        if (records.isEmpty()) return LongArray(numberOfGroups) { 0L }.toList()

        return ranges
            .map { (start, end) ->
                getRecordsFromRange(records, start, end).map { record ->
                    clampToRange(record, start, end)
                }
            }
            .map(::mapToDuration)
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
    ): List<Pair<Long, Long>> {
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
            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis
            rangeStart to rangeEnd
        }
    }

    private fun getWeeklyGrouping(
        numberOfWeeks: Int
    ): List<Pair<Long, Long>> {
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
            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.DATE, 7) }.timeInMillis
            rangeStart to rangeEnd
        }
    }

    private fun getMonthlyGrouping(
        numberOfMonths: Int
    ): List<Pair<Long, Long>> {
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
            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis
            rangeStart to rangeEnd
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