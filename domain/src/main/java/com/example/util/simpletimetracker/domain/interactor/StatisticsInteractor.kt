package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.mapper.CoveredRangeMapper
import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.Statistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class StatisticsInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val coveredRangeMapper: CoveredRangeMapper,
    private val statisticsMapper: StatisticsMapper
) {

    suspend fun getAll(): List<Statistics> {
        return recordInteractor.getAll()
            .groupBy { it.typeId }
            .map { entry ->
                Statistics(
                    typeId = entry.key,
                    duration = entry.value.let(statisticsMapper::mapToDuration)
                )
            }
    }

    suspend fun getFromRange(start: Long, end: Long, addUntracked: Boolean): List<Statistics> =
        withContext(Dispatchers.IO) {
            var untrackedTime = 0L
            recordInteractor.getFromRange(start, end)
                .also { records ->
                    if (addUntracked) untrackedTime = calculateUntracked(records, start, end)
                }
                .groupBy { it.typeId }
                .map { entry ->
                    Statistics(
                        typeId = entry.key,
                        duration = statisticsMapper.mapToDurationFromRange(entry.value, start, end)
                    )
                }
                .apply {
                    if (addUntracked && untrackedTime > 0L) {
                        this as MutableList
                        add(
                            Statistics(
                                typeId = -1L,
                                duration = untrackedTime
                            )
                        )
                    }
                }
        }

    private fun calculateUntracked(records: List<Record>, start: Long, end: Long): Long {
        // Bound end range of calculation to current time,
        // to not show untracked time in the future
        val todayEnd = System.currentTimeMillis()

        val untrackedTimeEndRange = min(todayEnd, end)
        if (start > untrackedTimeEndRange) return 0L

        return records
            // Remove parts of the record that are not in the range
            .map { max(it.timeStarted, start) to min(it.timeEnded, untrackedTimeEndRange) }
            // Calculate covered range
            .let(coveredRangeMapper::map)
            // Calculate uncovered range
            .let { untrackedTimeEndRange - start - it }
    }
}