package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.StatisticsCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class StatisticsCategoryInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor
) {

    // TODO simplify mappers?
    suspend fun getAll(): List<StatisticsCategory> = withContext(Dispatchers.IO) {
        val recordTypeCategories = recordTypeCategoryInteractor.getAll()
            .map { it.recordTypeId to it.categoryId }.toMap()

        recordInteractor.getAll()
            .mapNotNull { record ->
                (recordTypeCategories[record.typeId] ?: return@mapNotNull null) to record
            }
            .groupBy { (categoryId, _) -> categoryId }
            .mapValues { entry -> entry.value.map { it.second } }
            .map { (categoryId, records) ->
                StatisticsCategory(
                    categoryId = categoryId,
                    duration = records.let(::mapToDuration)
                )
            }
    }

    suspend fun getFromRange(start: Long, end: Long): List<StatisticsCategory> = withContext(Dispatchers.IO) {
        val recordTypeCategories = recordTypeCategoryInteractor.getAll()
            .map { it.recordTypeId to it.categoryId }.toMap()

        recordInteractor.getFromRange(start, end)
            .mapNotNull { record ->
                (recordTypeCategories[record.typeId] ?: return@mapNotNull null) to record
            }
            .groupBy { (categoryId, _) -> categoryId }
            .mapValues { entry -> entry.value.map { it.second } }
            .map { (categoryId, records) ->
                StatisticsCategory(
                    categoryId = categoryId,
                    duration = mapToDurationFromRange(records, start, end)
                )
            }
    }

    // TODO move to mapper and from statistics interactor
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