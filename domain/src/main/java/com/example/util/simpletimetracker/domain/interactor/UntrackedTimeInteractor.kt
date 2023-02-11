package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.mapper.UntrackedRecordMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject

class UntrackedTimeInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val untrackedRecordMapper: UntrackedRecordMapper,
) {

    suspend fun getUntrackedFromRange(start: Long, end: Long): List<Record> {
        val recordRanges = recordInteractor.getFromRange(start, end).map {
            Range(
                timeStarted = it.timeStarted,
                timeEnded = it.timeEnded,
            )
        }
        val runningRecordRanges = runningRecordInteractor.getAll().map {
            Range(
                timeStarted = it.timeStarted,
                timeEnded = System.currentTimeMillis(),
            )
        }

        return (recordRanges + runningRecordRanges).let {
            untrackedRecordMapper.mapToUntrackedRecords(
                records = it,
                start = start,
                end = end
            )
        }
    }
}