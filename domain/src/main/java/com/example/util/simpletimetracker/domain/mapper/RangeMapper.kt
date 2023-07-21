package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.MultitaskRecord
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class RangeMapper @Inject constructor() {

    fun getRecordsFromRange(
        records: List<RecordBase>,
        range: Range,
    ): List<RecordBase> {
        return records.filter { it.isInRange(range) }
    }

    fun getRunningRecordsFromRange(
        records: List<RunningRecord>,
        range: Range,
    ): List<RunningRecord> {
        return records.filter { it.isInRange(range) }
    }

    fun isRecordInRange(
        record: RecordBase,
        range: Range,
    ): Boolean {
        return record.isInRange(range)
    }

    fun clampToRange(
        record: RecordBase,
        range: Range,
    ): Range {
        return Range(
            timeStarted = max(record.timeStarted, range.timeStarted),
            timeEnded = min(record.timeEnded, range.timeEnded)
        )
    }

    fun mapRunningRecordToRecord(
        record: RunningRecord,
    ): Record {
        return Record(
            typeId = record.id,
            timeStarted = record.timeStarted,
            timeEnded = record.timeEnded,
            comment = record.comment,
            tagIds = record.tagIds,
        )
    }

    fun clampRecordToRange(
        record: RecordBase,
        range: Range,
    ): RecordBase {
        return if (!record.isCompletelyRange(range)) {
            when (record) {
                is Record -> clampNormalRecordToRange(record, range)
                is RunningRecord -> clampNormalRecordToRange(mapRunningRecordToRecord(record), range)
                is MultitaskRecord -> MultitaskRecord(
                    records = record.records.map {
                        clampNormalRecordToRange(it, range)
                    }
                )
            }
        } else {
            record
        }
    }

    fun mapToDuration(ranges: List<Range>): Long {
        return ranges.sumOf { it.duration }
    }

    private fun clampNormalRecordToRange(
        record: Record,
        range: Range,
    ): Record {
        return record.copy(
            timeStarted = max(record.timeStarted, range.timeStarted),
            timeEnded = min(record.timeEnded, range.timeEnded)
        )
    }

    private fun RecordBase.isInRange(
        range: Range,
    ): Boolean {
        return this.timeStarted < range.timeEnded && this.timeEnded > range.timeStarted
    }

    private fun RecordBase.isCompletelyRange(
        range: Range,
    ): Boolean {
        return this.timeStarted >= range.timeStarted && this.timeEnded <= range.timeEnded
    }
}