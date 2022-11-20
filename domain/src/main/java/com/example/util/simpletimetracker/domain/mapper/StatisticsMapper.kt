package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

class StatisticsMapper @Inject constructor() {

    fun mapToDuration(records: List<Record>): Long {
        return records.sumOf { it.timeEnded - it.timeStarted }
    }

    fun mapToDurationFromRange(records: List<Record>, start: Long, end: Long): Long {
        // Remove parts of the record that is not in the range
        return records.sumOf { min(it.timeEnded, end) - max(it.timeStarted, start) }
    }

    fun getDurationPercentString(
        sumDuration: Long,
        duration: Long,
        statisticsSize: Int
    ): String {
        val durationPercent = if (sumDuration != 0L) {
            duration * 100f / sumDuration
        } else {
            100f / statisticsSize
        }.roundToLong()

        return if (durationPercent == 0L) {
            "<1%"
        } else {
            "$durationPercent%"
        }
    }
}