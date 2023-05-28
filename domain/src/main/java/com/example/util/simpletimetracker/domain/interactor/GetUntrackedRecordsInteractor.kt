package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.mapper.UntrackedRecordMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject

class GetUntrackedRecordsInteractor @Inject constructor(
    private val untrackedRecordMapper: UntrackedRecordMapper,
    private val recordInteractor: RecordInteractor,
) {

    suspend fun get(
        range: Range,
        records: List<Range>,
    ): List<Record> {
        // Calculate from first record. No records - don't calculate.
        val minStart = recordInteractor.getNext(0)?.timeStarted ?: return emptyList()
        // Bound end range of calculation to current time,
        // to not show untracked time in the future
        val maxEnd = System.currentTimeMillis()

        // If range is all records - calculate from first records to current time.
        val actualRange = if (range.timeStarted == 0L && range.timeEnded == 0L) {
            Range(timeStarted = minStart, timeEnded = maxEnd)
        } else {
            range
        }
        return untrackedRecordMapper.calculateUntrackedRanges(
            records = records,
            range = actualRange,
            minStart = minStart,
            maxEnd = maxEnd,
        ).map {
            Record(
                typeId = UNTRACKED_ITEM_ID,
                timeStarted = it.first,
                timeEnded = it.second,
                comment = ""
            )
        }
    }
}