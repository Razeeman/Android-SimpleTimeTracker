package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordBase
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

class StatisticsMapper @Inject constructor() {

    fun mapToDuration(records: List<RecordBase>): Long {
        return records.sumOf(RecordBase::duration)
    }

    fun mapToDurationFromRange(records: List<RecordBase>, range: Range): Long {
        // Remove parts of the record that is not in the range
        return records.sumOf {
            min(it.timeEnded, range.timeEnded) - max(it.timeStarted, range.timeStarted)
        }
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