package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
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
                filterRecords = { records -> records.filter { it.typeId == typeId } },
                runningRecords = runningRecords.filter { it.id == typeId },
                range = range,
                rangeRecords = rangeRecords,
            )
        }
    }

    suspend fun getAllTagCurrents(
        tagIds: List<Long>,
        runningRecords: List<RunningRecord>,
        rangeLength: RangeLength,
    ): Map<Long, Result> {
        val range = getRange(rangeLength)
        // TODO TAG GOAL improve records load for big ranges (month)?
        val rangeRecords = recordInteractor.getFromRange(range)

        return tagIds.associateWith { tagId ->
            getRangeCurrent(
                filterRecords = { records ->
                    records.filter { record -> record.tags.any { it.tagId == tagId } }
                },
                runningRecords = runningRecords.filter { record ->
                    record.tags.any { it.tagId in tagIds }
                },
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
        return getRangeCurrent(
            typeId = typeId,
            runningRecords = listOfNotNull(runningRecord),
            rangeLength = rangeLength,
        )
    }

    private suspend fun getRangeCurrent(
        typeId: Long,
        runningRecords: List<RunningRecord>,
        rangeLength: RangeLength,
    ): Result {
        val range = getRange(rangeLength)
        val rangeRecords = getRangeRecords(
            rangeLength = rangeLength,
            range = range,
            typeIds = listOf(typeId),
        )

        return getRangeCurrent(
            filterRecords = { records -> records.filter { it.typeId == typeId } },
            runningRecords = runningRecords,
            range = range,
            rangeRecords = rangeRecords,
        )
    }

    private fun getRangeCurrent(
        filterRecords: (List<Record>) -> List<Record>,
        runningRecords: List<RunningRecord>,
        range: Range,
        rangeRecords: List<Record>,
    ): Result {
        val current = System.currentTimeMillis()
        val currentRunning = runningRecords.sumOf { runningRecord ->
            current - runningRecord.timeStarted
        }
        val currentRunningClamped = runningRecords.sumOf { runningRecord ->
            current - max(runningRecord.timeStarted, range.timeStarted)
        }
        val currentRunningCount = runningRecords.size

        val records = filterRecords(rangeRecords)
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