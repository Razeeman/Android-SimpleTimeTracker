package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

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
}