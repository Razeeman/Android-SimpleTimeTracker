package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

class StatisticsMapper @Inject constructor() {

    fun mapToDuration(records: List<Record>): Long {
        return records
            .map { it.timeEnded - it.timeStarted }
            .sum()
    }

    fun mapToDurationFromRange(records: List<Record>, start: Long, end: Long): Long {
        return records
            // Remove parts of the record that is not in the range
            .map { min(it.timeEnded, end) - max(it.timeStarted, start) }
            .sum()
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