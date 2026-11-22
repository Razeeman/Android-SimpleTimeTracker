package com.example.util.simpletimetracker.domain.statistics.interactor

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.toRange
import com.example.util.simpletimetracker.domain.record.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor.GetParam
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.mapper.DurationMapper
import com.example.util.simpletimetracker.domain.statistics.model.Statistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsInteractor @Inject constructor(
    private val durationMapper: DurationMapper,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getUntrackedRecordsInteractor: GetUntrackedRecordsInteractor,
    private val rangeMapper: RangeMapper,
) {

    suspend fun getFromRange(
        range: Range,
        addUntracked: Boolean,
        showSeconds: Boolean,
    ): List<Statistics> = withContext(Dispatchers.IO) {
        val records = getRecords(range)
        val untrackedRecords = getUntracked(
            range = range,
            records = records,
            addUntracked = addUntracked,
            showSeconds = showSeconds,
        )
        val recordsMap = getActivityRecords(records + untrackedRecords)
        getStatistics(
            range = range,
            records = recordsMap,
            showSeconds = showSeconds,
        )
    }

    suspend fun getRecords(range: Range): List<RecordBase> {
        val runningRecords = runningRecordInteractor.getAll()

        return if (rangeIsAllRecords(range)) {
            recordInteractor.getAll() + runningRecords
        } else {
            recordInteractor.getWithParams(GetParam.FromRange(range)) +
                rangeMapper.getRunningRecordsFromRange(runningRecords, range)
        }
    }

    private fun getActivityRecords(
        allRecords: List<RecordBase>,
    ): Map<Long, List<RecordBase>> {
        return allRecords.groupBy {
            it.typeIds.firstOrNull().orZero() // Multitask is not available in statistics.
        }
    }

    fun getActivityRecordsFull(
        allRecords: List<RecordBase>,
    ): Map<Long, List<RecordBase>> {
        val activities: MutableMap<Long, MutableList<RecordBase>> = mutableMapOf()

        allRecords.forEach { record ->
            record.typeIds.forEach { typeId ->
                activities.getOrPut(typeId) { mutableListOf() }.add(record)
            }
        }

        return activities
    }

    fun getStatistics(
        range: Range,
        records: Map<Long, List<RecordBase>>,
        showSeconds: Boolean,
    ): List<Statistics> {
        return records.map { (id, records) ->
            Statistics(
                id = id,
                data = getStatisticsData(
                    range = range,
                    records = records,
                    showSeconds = showSeconds,
                ),
            )
        }
    }

    fun getStatisticsData(
        allRecords: Map<Long, List<RecordBase>>,
        showSeconds: Boolean,
    ): List<Statistics> {
        return allRecords.map { (id, records) ->
            Statistics(
                id = id,
                data = mapRecords(records, showSeconds),
            )
        }
    }

    private fun getStatisticsData(
        range: Range,
        records: List<RecordBase>,
        showSeconds: Boolean,
    ): Statistics.Data {
        // If range is all records - do not clamp to range.
        return if (rangeIsAllRecords(range)) {
            mapRecords(records, showSeconds)
        } else {
            // Remove parts of the record that is not in the range
            val recordsFromRange = rangeMapper.getRecordsFromRange(records, range)
            mapRanges(
                ranges = recordsFromRange.map { rangeMapper.clampToRange(it, range) },
                showSeconds = showSeconds,
                runningCount = recordsFromRange.count { it is RunningRecord }.toLong(),
            )
        }
    }

    suspend fun getUntracked(
        range: Range,
        records: List<RecordBase>,
        addUntracked: Boolean,
        showSeconds: Boolean,
    ): List<RecordBase> {
        if (!addUntracked) return emptyList()

        val untrackedRanges = getUntrackedRecordsInteractor.get(
            range = range,
            records = records.map(RecordBase::toRange),
        )
        val untrackedTime = untrackedRanges.sumOf {
            durationMapper.map(
                timeStarted = it.timeStarted,
                timeEnded = it.timeEnded,
                showSeconds = showSeconds,
            )
        }
        return if (untrackedTime > 0L) untrackedRanges else emptyList()
    }

    private fun rangeIsAllRecords(range: Range): Boolean {
        return range.isUndefined
    }

    private fun mapRecords(
        records: List<RecordBase>,
        showSeconds: Boolean,
    ): Statistics.Data {
        return Statistics.Data(
            duration = records.sumOf {
                durationMapper.map(
                    timeStarted = it.timeStarted,
                    timeEnded = it.timeEnded,
                    showSeconds = showSeconds,
                )
            },
            count = records.size.toLong(),
            runningCount = records.count { it is RunningRecord }.toLong(),
        )
    }

    private fun mapRanges(
        ranges: List<Range>,
        showSeconds: Boolean,
        runningCount: Long,
    ): Statistics.Data {
        return Statistics.Data(
            duration = ranges.sumOf {
                durationMapper.map(
                    timeStarted = it.timeStarted,
                    timeEnded = it.timeEnded,
                    showSeconds = showSeconds,
                )
            },
            count = ranges.size.toLong(),
            runningCount = runningCount,
        )
    }
}