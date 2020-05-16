package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsInteractor @Inject constructor(
    private val recordRepo: RecordRepo
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

    private fun mapToDuration(records: List<Record>): Long {
        return records
            .map { it.timeEnded - it.timeStarted }
            .sum()
    }
}