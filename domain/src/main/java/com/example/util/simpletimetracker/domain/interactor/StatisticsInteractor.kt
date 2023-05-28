package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.Statistics
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatisticsInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getUntrackedRecordsInteractor: GetUntrackedRecordsInteractor,
    private val statisticsMapper: StatisticsMapper,
) {

    suspend fun getAllRunning(): List<Statistics> {
        return runningRecordInteractor.getAll()
            .groupBy { it.id }
            .map { entry ->
                Statistics(
                    id = entry.key,
                    duration = entry.value.let(statisticsMapper::mapToRunningDuration)
                )
            }
    }

    suspend fun getFromRange(
        range: Range,
        addUntracked: Boolean,
    ): List<Statistics> = withContext(Dispatchers.IO) {
        val records = getRecords(range)

        records
            .groupBy { it.typeId }
            .let {
                getStatistics(range, it)
            }
            .plus(
                getUntracked(range, records, addUntracked)
            )
    }

    suspend fun getRecords(range: Range): List<Record> {
        return if (rangeIsAllRecords(range)) {
            recordInteractor.getAll()
        } else {
            recordInteractor.getFromRange(range.timeStarted, range.timeEnded)
        }
    }

    fun getStatistics(
        range: Range,
        records: Map<Long, List<Record>>,
    ): List<Statistics> {
        return records.map { (id, records) ->
            Statistics(id, getDuration(range, records))
        }
    }

    fun getDuration(
        range: Range,
        records: List<Record>,
    ): Long {
        // If range is all records - do not clamp to range.
        return if (rangeIsAllRecords(range)) {
            statisticsMapper.mapToDuration(records)
        } else {
            statisticsMapper.mapToDurationFromRange(records, range)
        }
    }

    suspend fun getUntracked(
        range: Range,
        records: List<Record>,
        addUntracked: Boolean,
    ): List<Statistics> {
        if (addUntracked) {
            val untrackedTime = getUntrackedRecordsInteractor.get(
                range = range,
                records = records.map { Range(it.timeStarted, it.timeEnded) }
            ).sumOf { it.duration }

            if (untrackedTime > 0L) {
                return Statistics(
                    id = UNTRACKED_ITEM_ID,
                    duration = untrackedTime
                ).let(::listOf)
            }
        }

        return emptyList()
    }

    private fun rangeIsAllRecords(range: Range): Boolean {
        return range.timeStarted == 0L && range.timeEnded == 0L
    }
}