package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.Range
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class UntrackedRecordMapper @Inject constructor(
    private val unCoveredRangesMapper: UnCoveredRangesMapper,
) {

    fun calculateUntrackedRanges(
        records: List<Range>,
        range: Range,
        minStart: Long,
        maxEnd: Long,
    ): List<Pair<Long, Long>> {
        val untrackedTimeStart = max(minStart, range.timeStarted)
        if (range.timeEnded < untrackedTimeStart) return emptyList()
        val untrackedTimeEndRange = min(maxEnd, range.timeEnded)
        if (range.timeStarted > untrackedTimeEndRange) return emptyList()

        return records
            // Remove parts of the record that are not in the range
            .map { max(it.timeStarted, untrackedTimeStart) to min(it.timeEnded, untrackedTimeEndRange) }
            // Calculate uncovered ranges
            .let { unCoveredRangesMapper.map(untrackedTimeStart, untrackedTimeEndRange, it) }
            .filter {
                // Filter only untracked records that are longer than a minute
                (it.second - it.first) >= UNTRACKED_RECORD_LENGTH_LIMIT
            }
    }

    companion object {
        private const val UNTRACKED_RECORD_LENGTH_LIMIT: Long = 60 * 1000L // 1 min
    }
}