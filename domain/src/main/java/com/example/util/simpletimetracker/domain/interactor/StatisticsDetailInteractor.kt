package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.StatisticsDetail
import javax.inject.Inject

class StatisticsDetailInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor
) {

    suspend fun get(typeId: Long): StatisticsDetail {
        val records = recordInteractor.getAll() // TODO get by typeId
            .filter { it.typeId == typeId }

        val totalDuration = records.let(::mapToDuration)
        val timesTracked = records.size.toLong()

        return StatisticsDetail(
            typeId = typeId,
            totalDuration = totalDuration,
            timesTracked = timesTracked
        )
    }

    private fun mapToDuration(records: List<Record>): Long {
        return records
            .map { it.timeEnded - it.timeStarted }
            .sum()
    }
}