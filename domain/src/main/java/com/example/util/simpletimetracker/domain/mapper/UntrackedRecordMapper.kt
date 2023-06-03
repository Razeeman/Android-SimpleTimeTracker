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
        durationCutoff: Long,
    ): List<Range> {
        val untrackedTimeStart = max(minStart, range.timeStarted)
        if (range.timeEnded < untrackedTimeStart) return emptyList()
        val untrackedTimeEndRange = min(maxEnd, range.timeEnded)
        if (range.timeStarted > untrackedTimeEndRange) return emptyList()

        return records
            // Remove parts of the record that are not in the range
            .map {
                Range(
                    max(it.timeStarted, untrackedTimeStart),
                    min(it.timeEnded, untrackedTimeEndRange)
                )
            }
            // Calculate uncovered ranges
            .let { unCoveredRangesMapper.map(untrackedTimeStart, untrackedTimeEndRange, it) }
            .filter { filter(it.duration, durationCutoff) }
    }

    fun filter(
        duration: Long,
        durationCutoff: Long,
    ): Boolean {
        return if (durationCutoff > 0) {
            // Filter only untracked records that are longer than a cutoff
            duration >= durationCutoff * 1000
        } else {
            true
        }
    }
}