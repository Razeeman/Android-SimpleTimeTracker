package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.Statistics
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatisticsCategoryInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsMapper: StatisticsMapper,
) {

    suspend fun getAll(): List<Statistics> = withContext(Dispatchers.IO) {
        val allRecords = recordInteractor.getAll()

        getCategoryRecords(allRecords)
            .map { (categoryId, records) ->
                Statistics(
                    id = categoryId,
                    duration = records.let(statisticsMapper::mapToDuration)
                )
            }
        // TODO add addUncategorized
    }

    suspend fun getFromRange(
        range: Range,
        addUntracked: Boolean,
        addUncategorized: Boolean,
    ): List<Statistics> = withContext(Dispatchers.IO) {
        val allRecords = recordInteractor.getFromRange(range.timeStarted, range.timeEnded)

        getCategoryRecords(allRecords)
            .map { (categoryId, records) ->
                Statistics(
                    id = categoryId,
                    duration = statisticsMapper.mapToDurationFromRange(records, range)
                )
            }
            .apply {
                if (addUntracked) {
                    val untrackedTime = statisticsInteractor
                        .calculateUntracked(allRecords, range)
                    if (untrackedTime > 0L) {
                        this as MutableList
                        Statistics(
                            id = UNTRACKED_ITEM_ID,
                            duration = untrackedTime
                        ).let(::add)
                    }
                }
            }
            .apply {
                if (addUncategorized) {
                    val uncategorizedTime = getUncategorized(allRecords)
                        .let(statisticsMapper::mapToDuration) // TODO from range (remove parts not in range).
                    if (uncategorizedTime > 0L) {
                        this as MutableList
                        Statistics(
                            id = UNCATEGORIZED_ITEM_ID,
                            duration = uncategorizedTime,
                        ).let(::add)
                    }
                }
            }
    }

    private suspend fun getCategoryRecords(allRecords: List<Record>): Map<Long, List<Record>> {
        val recordTypeCategories = recordTypeCategoryInteractor.getAll()
            .groupBy(RecordTypeCategory::categoryId)
            .mapValues { it.value.map(RecordTypeCategory::recordTypeId) }

        return recordTypeCategories
            .mapValues { (_, typeIds) -> allRecords.filter { it.typeId in typeIds } }
            .filterValues(List<Record>::isNotEmpty)
    }

    private suspend fun getUncategorized(allRecords: List<Record>): List<Record> {
        val recordTypeCategories = recordTypeCategoryInteractor.getAll().map { it.recordTypeId }

        return allRecords.filter { it.typeId !in recordTypeCategories }
    }
}