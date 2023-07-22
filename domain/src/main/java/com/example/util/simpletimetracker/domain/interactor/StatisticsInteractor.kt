package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.Statistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getUntrackedRecordsInteractor: GetUntrackedRecordsInteractor,
    private val rangeMapper: RangeMapper,
) {

    suspend fun getFromRange(
        range: Range,
        addUntracked: Boolean,
    ): List<Statistics> = withContext(Dispatchers.IO) {
        val records = getRecords(range)

        records
            .groupBy {
                it.typeIds.firstOrNull().orZero() // Multitask is not available in statistics.
            }
            .let {
                getStatistics(range, it)
            }
            .plus(
                getUntracked(range, records, addUntracked),
            )
    }

    suspend fun getRecords(range: Range): List<RecordBase> {
        val runningRecords = runningRecordInteractor.getAll()

        return if (rangeIsAllRecords(range)) {
            recordInteractor.getAll() + runningRecords
        } else {
            recordInteractor.getFromRange(range) +
                rangeMapper.getRunningRecordsFromRange(runningRecords, range)
        }
    }

    fun getStatistics(
        range: Range,
        records: Map<Long, List<RecordBase>>,
    ): List<Statistics> {
        return records.map { (id, records) ->
            Statistics(
                id = id,
                data = getStatisticsData(range, records),
            )
        }
    }

    fun getStatisticsData(
        range: Range,
        records: List<RecordBase>,
    ): Statistics.Data {
        // If range is all records - do not clamp to range.
        return if (rangeIsAllRecords(range)) {
            Statistics.Data(
                duration = records.sumOf(RecordBase::duration),
                count = records.size.toLong(),
            )
        } else {
            // Remove parts of the record that is not in the range
            rangeMapper.getRecordsFromRange(records, range)
                .map { rangeMapper.clampToRange(it, range) }
                .let {
                    Statistics.Data(
                        duration = it.sumOf(Range::duration),
                        count = it.size.toLong(),
                    )
                }
        }
    }

    suspend fun getUntracked(
        range: Range,
        records: List<RecordBase>,
        addUntracked: Boolean,
    ): List<Statistics> {
        if (addUntracked) {
            val untrackedRanges = getUntrackedRecordsInteractor.get(
                range = range,
                records = records.map { Range(it.timeStarted, it.timeEnded) },
            )
            val untrackedTime = untrackedRanges.sumOf { it.duration }
            val untrackedCount = untrackedRanges.size

            if (untrackedTime > 0L) {
                return Statistics(
                    id = UNTRACKED_ITEM_ID,
                    data = Statistics.Data(
                        duration = untrackedTime,
                        count = untrackedCount.toLong(),
                    ),
                ).let(::listOf)
            }
        }

        return emptyList()
    }

    private fun rangeIsAllRecords(range: Range): Boolean {
        return range.timeStarted == 0L && range.timeEnded == 0L
    }
}