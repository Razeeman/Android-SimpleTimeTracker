package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import java.lang.Long.max
import javax.inject.Inject

class GetCurrentRecordsDurationInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val rangeMapper: RangeMapper,
    private val getRangeInteractor: GetRangeInteractor,
) {

    suspend fun getDailyCurrent(
        typeId: Long,
        runningRecord: RunningRecord?,
    ): Result {
        return getRangeCurrent(typeId, runningRecord, getRange(RangeLength.Day))
    }

    suspend fun getDailyCurrent(runningRecord: RunningRecord): Result {
        return getRangeCurrent(runningRecord.id, runningRecord, getRange(RangeLength.Day))
    }

    suspend fun getWeeklyCurrent(runningRecord: RunningRecord): Result {
        return getRangeCurrent(runningRecord.id, runningRecord, getRange(RangeLength.Week))
    }

    suspend fun getMonthlyCurrent(runningRecord: RunningRecord): Result {
        return getRangeCurrent(runningRecord.id, runningRecord, getRange(RangeLength.Month))
    }

    suspend fun getAllDailyCurrents(
        typesMap: Map<Long, RecordType>,
        runningRecords: List<RunningRecord>,
    ): Map<Long, Result> {
        val range = getRange(RangeLength.Day)
        val rangeRecords = recordInteractor.getFromRange(range)

        return typesMap.map { (typeId, _) ->
            typeId to getRangeCurrent(
                typeId = typeId,
                runningRecord = runningRecords.firstOrNull { it.id == typeId },
                range = range,
                rangeRecords = rangeRecords,
            )
        }.toMap()
    }

    private suspend fun getRangeCurrent(
        typeId: Long,
        runningRecord: RunningRecord?,
        range: Range,
    ): Result {
        return getRangeCurrent(
            typeId = typeId,
            runningRecord = runningRecord,
            range = range,
            rangeRecords = recordInteractor.getFromRangeByType(
                typeIds = listOf(typeId),
                range = range,
            ),
        )
    }

    private fun getRangeCurrent(
        typeId: Long,
        runningRecord: RunningRecord?,
        range: Range,
        rangeRecords: List<Record>,
    ): Result {
        val current = System.currentTimeMillis()
        val currentRunning = if (runningRecord != null) {
            current - runningRecord.timeStarted
        } else {
            0
        }
        val currentRunningClamped = if (runningRecord != null) {
            current - max(runningRecord.timeStarted, range.timeStarted)
        } else {
            0
        }
        val currentRunningCount = if (runningRecord != null) 1 else 0

        val records = rangeRecords
            .filter { it.typeId == typeId }
            .map { rangeMapper.clampToRange(it, range) }
        val duration = records
            .let(rangeMapper::mapToDuration)
        val count = records.size.toLong()

        return Result(
            range = range,
            duration = duration + currentRunningClamped,
            count = count + currentRunningCount,
            durationDiffersFromCurrent = duration != 0L || currentRunning != currentRunningClamped,
        )
    }

    private suspend fun getRange(rangeLength: RangeLength): Range {
        return getRangeInteractor.getRange(rangeLength)
    }

    data class Result(
        val range: Range,
        val duration: Long,
        val count: Long,
        val durationDiffersFromCurrent: Boolean,
    )
}