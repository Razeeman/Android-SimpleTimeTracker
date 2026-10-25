package com.example.util.simpletimetracker.domain.statistics.mapper

import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import javax.inject.Inject

class StatisticsDurationWithoutOverlapsMapper @Inject constructor() {

    fun map(
        records: Sequence<RecordBase>,
    ): Long {
        val intervals = records
            .filterNot { UNTRACKED_ITEM_ID in it.typeIds }
            .map { Range(it.timeStarted, it.timeEnded) }
            .filter { it.timeEnded > it.timeStarted }
            .sortedBy(Range::timeStarted)
            .iterator()
        if (!intervals.hasNext()) return 0

        val first = intervals.next()
        var start = first.timeStarted
        var end = first.timeEnded
        var total = 0L
        for (interval in intervals) {
            if (interval.timeStarted <= end) {
                end = maxOf(end, interval.timeEnded)
            } else {
                total += end - start
                start = interval.timeStarted
                end = interval.timeEnded
            }
        }
        return total + (end - start)
    }
}
