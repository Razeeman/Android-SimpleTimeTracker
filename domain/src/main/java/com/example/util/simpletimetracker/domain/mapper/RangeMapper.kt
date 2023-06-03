package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class RangeMapper @Inject constructor() {

    fun getRecordsFromRange(
        records: List<Record>,
        range: Range,
    ): List<Record> {
        return records.filter { it.isInRange(range) }
    }

    fun getRunningRecordsFromRange(
        records: List<RunningRecord>,
        range: Range,
    ): List<RunningRecord> {
        return records.filter { it.isInRange(range) }
    }

    fun clampToRange(
        record: Record,
        rangeStart: Long,
        rangeEnd: Long,
    ): Range {
        return Range(
            timeStarted = max(record.timeStarted, rangeStart),
            timeEnded = min(record.timeEnded, rangeEnd)
        )
    }

    fun clampRecordToRange(
        record: Record,
        range: Range,
    ): Record {
        return if (!record.isCompletelyRange(range)) {
            record.copy(
                timeStarted = max(record.timeStarted, range.timeStarted),
                timeEnded = min(record.timeEnded, range.timeEnded)
            )
        } else {
            record
        }
    }

    fun mapToDuration(ranges: List<Range>): Long {
        return ranges.sumOf { it.duration }
    }

    private fun RecordBase.isInRange(
        range: Range,
    ): Boolean {
        return this.timeStarted < range.timeEnded && this.timeEnded > range.timeStarted
    }

    private fun Record.isCompletelyRange(
        range: Range,
    ): Boolean {
        return this.timeStarted >= range.timeStarted && this.timeEnded <= range.timeEnded
    }
}