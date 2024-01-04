package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
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
        return getRangeCurrent(typeId, runningRecord, RangeLength.Day)
    }

    suspend fun getDailyCurrent(runningRecord: RunningRecord): Result {
        return getRangeCurrent(runningRecord.id, runningRecord, RangeLength.Day)
    }

    suspend fun getWeeklyCurrent(runningRecord: RunningRecord): Result {
        return getRangeCurrent(runningRecord.id, runningRecord, RangeLength.Week)
    }

    suspend fun getMonthlyCurrent(runningRecord: RunningRecord): Result {
        return getRangeCurrent(runningRecord.id, runningRecord, RangeLength.Month)
    }

    suspend fun getAllCurrents(
        typeIds: List<Long>,
        runningRecords: List<RunningRecord>,
        rangeLength: RangeLength,
    ): Map<Long, Result> {
        val range = getRange(rangeLength)
        val rangeRecords = getRangeRecords(
            rangeLength = rangeLength,
            range = range,
            typeIds = typeIds,
        )

        return typeIds.associateWith { typeId ->
            getRangeCurrent(
                typeId = typeId,
                runningRecord = runningRecords.firstOrNull { it.id == typeId },
                range = range,
                rangeRecords = rangeRecords,
            )
        }
    }

    suspend fun getAllDailyCurrents(
        typeIds: List<Long>,
        runningRecords: List<RunningRecord>,
    ): Map<Long, Result> {
        return getAllCurrents(
            typeIds = typeIds,
            runningRecords = runningRecords,
            rangeLength = RangeLength.Day,
        )
    }

    private suspend fun getRangeCurrent(
        typeId: Long,
        runningRecord: RunningRecord?,
        rangeLength: RangeLength,
    ): Result {
        val range = getRange(rangeLength)
        val rangeRecords = getRangeRecords(
            rangeLength = rangeLength,
            range = range,
            typeIds = listOf(typeId),
        )

        return getRangeCurrent(
            typeId = typeId,
            runningRecord = runningRecord,
            range = range,
            rangeRecords = rangeRecords,
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

    private suspend fun getRangeRecords(
        rangeLength: RangeLength,
        range: Range,
        typeIds: List<Long>,
    ): List<Record> {
        // Use getFromRange to hit cache.
        return if (rangeLength is RangeLength.Day) {
            recordInteractor.getFromRange(
                range = range,
            )
        } else {
            recordInteractor.getFromRangeByType(
                typeIds = typeIds,
                range = range,
            )
        }
    }

    data class Result(
        val range: Range,
        val duration: Long,
        val count: Long,
        val durationDiffersFromCurrent: Boolean,
    )
}