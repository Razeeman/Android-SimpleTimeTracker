package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.domain.repo.RecordCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class StatisticsInteractor @Inject constructor(
    private val recordRepo: RecordRepo,
    private val recordCacheRepo: RecordCacheRepo
) {

    suspend fun getAll(): List<Statistics> = withContext(Dispatchers.IO) {
        recordRepo.getAll()
            .groupBy { it.typeId }
            .map { entry ->
                Statistics(
                    typeId = entry.key,
                    duration = entry.value.let(::mapToDuration)
                )
            }
    }

    suspend fun getFromRange(start: Long, end: Long): List<Statistics> =
        withContext(Dispatchers.IO) {
            getRecords(start, end)
                .groupBy { it.typeId }
                .map { entry ->
                    Statistics(
                        typeId = entry.key,
                        duration = mapToDurationFromRange(entry.value, start, end)
                    )
                }
        }

    private suspend fun getRecords(start: Long, end: Long): List<Record> {
        return recordCacheRepo.getFromRange(start, end)
            ?: recordRepo.getFromRange(start, end)
                .also { recordCacheRepo.putWithRange(start, end, it) }
    }

    private fun mapToDuration(records: List<Record>): Long {
        return records
            .map { it.timeEnded - it.timeStarted }
            .sum()
    }

    private fun mapToDurationFromRange(records: List<Record>, start: Long, end: Long): Long {
        return records
            .map { min(it.timeEnded, end) - max(it.timeStarted, start) }
            .sum()
    }
}