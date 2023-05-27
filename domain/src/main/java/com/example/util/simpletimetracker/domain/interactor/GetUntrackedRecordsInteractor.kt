package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
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
        records: List<Record>,
    ): List<Record> {
        // If range is all records - calculate from first records to current time.
        val actualRange = if (range.timeStarted == 0L && range.timeEnded == 0L) {
            Range(
                timeStarted = recordInteractor.getNext(0)?.timeStarted.orZero(),
                timeEnded = System.currentTimeMillis()
            )
        } else {
            range
        }
        return untrackedRecordMapper.calculateUntrackedRanges(
            records = records.map { Range(it.timeStarted, it.timeEnded) },
            range = actualRange,
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