package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor.GetParam
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import java.lang.Long.max
import javax.inject.Inject

class GetCurrentRecordsDurationInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val rangeMapper: RangeMapper,
    private val getRangeInteractor: GetRangeInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    suspend fun getDailyCurrent(
        typeId: Long,
        runningRecord: RunningRecord?,
    ): Result {
        return getRangeCurrent(typeId, runningRecord, RangeLength.Day)
    }

    suspend fun getAllCurrents(
        typeIds: Set<Long>,
        runningRecords: List<RunningRecord>,
        rangeLength: RangeLength,
    ): Map<Long, Result> {
        val range = getRange(rangeLength)
        val rangeRecords = getRangeRecords(
            rangeLength = rangeLength,
            range = range,
            typeIds = typeIds,
        )

        return getRangeCurrents(
            targetIds = typeIds,
            getTargetIdsFromRecord = { record -> record.typeIds.asSequence() },
            allRunningRecords = runningRecords,
            range = range,
            rangeRecords = rangeRecords,
        )
    }

    suspend fun getAllCategoryCurrents(
        recordTypeCategories: Map<Long, List<Long>>,
        runningRecords: List<RunningRecord>,
        rangeLength: RangeLength,
    ): Map<Long, Result> {
        val range = getRange(rangeLength)
        val rangeRecords = getRangeRecords(
            rangeLength = rangeLength,
            range = range,
            typeIds = recordTypeCategories.values.flatten().toSet(),
        )

        val categoryIdsByTypeId = mutableMapOf<Long, MutableSet<Long>>()
        recordTypeCategories.forEach { (categoryId, typeIds) ->
            typeIds.forEach { typeId ->
                categoryIdsByTypeId.getOrPut(typeId, ::mutableSetOf).add(categoryId)
            }
        }

        return getRangeCurrents(
            targetIds = recordTypeCategories.keys,
            getTargetIdsFromRecord = { record ->
                record.typeIds.asSequence().flatMap { typeId ->
                    categoryIdsByTypeId[typeId].orEmpty().asSequence()
                }
            },
            allRunningRecords = runningRecords,
            range = range,
            rangeRecords = rangeRecords,
        )
    }

    suspend fun getAllTagCurrents(
        tagIds: List<Long>,
        runningRecords: List<RunningRecord>,
        rangeLength: RangeLength,
    ): Map<Long, Result> {
        val range = getRange(rangeLength)
        // TODO TAG GOAL improve records load for big ranges (month)?
        val rangeRecords = recordInteractor.getWithParams(GetParam.FromRange(range))

        val targetTagIds = tagIds.toSet()
        return getRangeCurrents(
            targetIds = targetTagIds,
            getTargetIdsFromRecord = { record ->
                record.tags.asSequence()
                    .map(RecordBase.Tag::tagId)
                    .filter(targetTagIds::contains)
            },
            allRunningRecords = runningRecords,
            range = range,
            rangeRecords = rangeRecords,
        )
    }

    suspend fun getAllDailyCurrents(
        typeIds: Set<Long>,
        runningRecords: List<RunningRecord>,
    ): Map<Long, Result> {
        return getAllCurrents(
            typeIds = typeIds,
            runningRecords = runningRecords,
            rangeLength = RangeLength.Day,
        )
    }

    suspend fun getRangeCurrent(
        typeId: Long,
        runningRecord: RunningRecord?,
        rangeLength: RangeLength,
    ): Result {
        val range = getRange(rangeLength)
        val rangeRecords = getRangeRecords(
            rangeLength = rangeLength,
            range = range,
            typeIds = setOf(typeId),
        )

        return getRangeCurrents(
            targetIds = setOf(typeId),
            getTargetIdsFromRecord = { record -> record.typeIds.asSequence() },
            allRunningRecords = listOfNotNull(runningRecord),
            range = range,
            rangeRecords = rangeRecords,
        ).getValue(typeId)
    }

    private fun getRangeCurrents(
        targetIds: Set<Long>,
        getTargetIdsFromRecord: (RecordBase) -> Sequence<Long>,
        allRunningRecords: List<RunningRecord>,
        range: Range,
        rangeRecords: List<Record>,
    ): Map<Long, Result> {
        val accumulators = targetIds.associateWith { Accumulator() }
        val current = currentTimestampProvider.get()

        rangeRecords.forEach { record ->
            var duration: Long? = null
            getTargetIdsFromRecord(record).distinct().forEach { targetId ->
                accumulators[targetId]?.let { accumulator ->
                    val recordDuration = duration ?: rangeMapper
                        .clampToRange(record, range)
                        .duration
                        .also { duration = it }
                    accumulator.duration += recordDuration
                    accumulator.count++
                }
            }
        }

        allRunningRecords.forEach { runningRecord ->
            var currentRunning: Long? = null
            var currentRunningClamped: Long? = null
            getTargetIdsFromRecord(runningRecord).distinct().forEach { targetId ->
                accumulators[targetId]?.let { accumulator ->
                    val runningDuration = currentRunning
                        ?: (current - runningRecord.timeStarted).also { currentRunning = it }
                    val clampedDuration = currentRunningClamped
                        ?: (current - max(runningRecord.timeStarted, range.timeStarted))
                            .also { currentRunningClamped = it }
                    accumulator.currentRunning += runningDuration
                    accumulator.currentRunningClamped += clampedDuration
                    accumulator.currentRunningCount++
                }
            }
        }

        return accumulators.mapValues { (_, accumulator) ->
            accumulator.toResult(range)
        }
    }

    private suspend fun getRange(rangeLength: RangeLength): Range {
        return getRangeInteractor.getRange(rangeLength)
    }

    private suspend fun getRangeRecords(
        rangeLength: RangeLength,
        range: Range,
        typeIds: Set<Long>,
    ): List<Record> {
        // Use getFromRange to hit cache.
        val params = if (rangeLength is RangeLength.Day) {
            GetParam.FromRange(range)
        } else {
            GetParam.FromRangeByType(typeIds, range)
        }
        return recordInteractor.getWithParams(params)
    }

    private fun Accumulator.toResult(range: Range): Result {
        return Result(
            range = range,
            duration = duration + currentRunningClamped,
            count = count + currentRunningCount,
            durationDiffersFromCurrent = duration != 0L || currentRunning != currentRunningClamped,
        )
    }

    data class Result(
        val range: Range,
        val duration: Long,
        val count: Long,
        val durationDiffersFromCurrent: Boolean,
    )

    private data class Accumulator(
        var duration: Long = 0,
        var count: Long = 0,
        var currentRunning: Long = 0,
        var currentRunningClamped: Long = 0,
        var currentRunningCount: Long = 0,
    )
}