package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.mapper.CoveredRangeMapper
import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.Statistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class StatisticsInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val coveredRangeMapper: CoveredRangeMapper,
    private val statisticsMapper: StatisticsMapper
) {

    suspend fun getAll(): List<Statistics> {
        return recordInteractor.getAll()
            .groupBy { it.typeId }
            .map { entry ->
                Statistics(
                    id = entry.key,
                    duration = entry.value.let(statisticsMapper::mapToDuration)
                )
            }
    }

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
        var untrackedTime = 0L
        recordInteractor.getFromRange(range.timeStarted, range.timeEnded)
            .also { records ->
                if (addUntracked) untrackedTime = calculateUntracked(records, range)
            }
            .groupBy { it.typeId }
            .map { entry ->
                Statistics(
                    id = entry.key,
                    duration = statisticsMapper.mapToDurationFromRange(entry.value, range)
                )
            }
            .apply {
                if (addUntracked && untrackedTime > 0L) {
                    this as MutableList
                    add(
                        Statistics(
                            id = UNTRACKED_ITEM_ID,
                            duration = untrackedTime
                        )
                    )
                }
            }
    }

    fun calculateUntracked(records: List<Record>, range: Range): Long {
        // Bound end range of calculation to current time,
        // to not show untracked time in the future
        val todayEnd = System.currentTimeMillis()

        val untrackedTimeEndRange = min(todayEnd, range.timeEnded)
        if (range.timeStarted > untrackedTimeEndRange) return 0L

        return records
            // Remove parts of the record that are not in the range
            .map { max(it.timeStarted, range.timeStarted) to min(it.timeEnded, untrackedTimeEndRange) }
            // Calculate covered range
            .let(coveredRangeMapper::map)
            // Calculate uncovered range
            .let { untrackedTimeEndRange - range.timeStarted - it }
    }
}