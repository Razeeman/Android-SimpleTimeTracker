package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.StatisticsCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsCategoryInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val statisticsMapper: StatisticsMapper
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
                    duration = records.let(statisticsMapper::mapToDuration)
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
                    duration = statisticsMapper.mapToDurationFromRange(records, start, end)
                )
            }
    }
}