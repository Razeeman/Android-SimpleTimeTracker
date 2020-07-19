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

    suspend fun getDurations(typeId: Long, numberOfDays: Int): List<Long> {
        if (numberOfDays == 0) return emptyList()

        val calendar = Calendar.getInstance()

        val ranges = (numberOfDays downTo 0).map { shift ->
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

        val records = recordInteractor.getFromRange(
            start = ranges.first().first,
            end = ranges.last().second
        ).filter { it.typeId == typeId }

        if (records.isEmpty()) return LongArray(numberOfDays) { 0L }.toList()

        return ranges
            .map { (start, end) ->
                getRecordsFromRange(records, start, end).map { record ->
                    clampToRange(record, start, end)
                }
            }
            .map(::mapToDuration)
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