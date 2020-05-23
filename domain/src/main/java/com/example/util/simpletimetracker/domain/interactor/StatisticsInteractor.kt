package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.mapper.CoveredRangeMapper
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.Statistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class StatisticsInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val coveredRangeMapper: CoveredRangeMapper
) {

    suspend fun getAll(): List<Statistics> {
        return recordInteractor.getAll()
            .groupBy { it.typeId }
            .map { entry ->
                Statistics(
                    typeId = entry.key,
                    duration = entry.value.let(::mapToDuration)
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
                        duration = mapToDurationFromRange(entry.value, start, end)
                    )
                }
                .apply {
                    if (addUntracked) {
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
        return records
            // Remove parts of the record that is not in the range
            .map { max(it.timeStarted, start) to min(it.timeEnded, end) }
            // Calculate covered range
            .let(coveredRangeMapper::map)
            // Calculate uncovered range
            .let { end - start - it }
    }

    private fun mapToDuration(records: List<Record>): Long {
        return records
            .map { it.timeEnded - it.timeStarted }
            .sum()
    }

    private fun mapToDurationFromRange(records: List<Record>, start: Long, end: Long): Long {
        return records
            // Remove parts of the record that is not in the range
            .map { min(it.timeEnded, end) - max(it.timeStarted, start) }
            .sum()
    }
}