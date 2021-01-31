package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class UntrackedRecordMapper @Inject constructor(
    private val unCoveredRangesMapper: UnCoveredRangesMapper
) {

    fun mapToUntrackedRecords(
        records: List<Record>,
        start: Long,
        end: Long
    ): List<Record> {
        return calculateUntrackedRanges(records, start, end).map {
            Record(
                typeId = -1,
                timeStarted = it.first,
                timeEnded = it.second,
                comment = ""
            )
        }
    }

    private fun calculateUntrackedRanges(
        records: List<Record>,
        start: Long,
        end: Long
    ): List<Pair<Long, Long>> {
        // Bound end range of calculation to current time,
        // to not show untracked time in the future
        val todayEnd = System.currentTimeMillis()

        val untrackedTimeEndRange = min(todayEnd, end)
        if (start > untrackedTimeEndRange) return emptyList()

        return records
            // TODO unnecessary clamp?
            // Remove parts of the record that are not in the range
            .map { max(it.timeStarted, start) to min(it.timeEnded, untrackedTimeEndRange) }
            // Calculate uncovered ranges
            .let { unCoveredRangesMapper.map(start, untrackedTimeEndRange, it) }
    }
}