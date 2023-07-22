package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.Statistics
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatisticsCategoryInteractor @Inject constructor(
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val statisticsInteractor: StatisticsInteractor,
) {

    suspend fun getFromRange(
        range: Range,
        addUntracked: Boolean,
        addUncategorized: Boolean,
    ): List<Statistics> = withContext(Dispatchers.IO) {
        val records = statisticsInteractor.getRecords(range)

        getCategoryRecords(records)
            .let {
                statisticsInteractor.getStatistics(range, it)
            }
            .plus(
                statisticsInteractor.getUntracked(range, records, addUntracked)
            )
            .plus(
                getUncategorized(range, records, addUncategorized)
            )
    }

    private suspend fun getUncategorized(
        range: Range,
        records: List<RecordBase>,
        addUncategorized: Boolean,
    ): List<Statistics> {
        if (addUncategorized) {
            val uncategorizedTime = statisticsInteractor.getStatisticsData(
                range = range,
                records = getUncategorized(records),
            )
            if (uncategorizedTime.duration > 0L) {
                return Statistics(
                    id = UNCATEGORIZED_ITEM_ID,
                    data = uncategorizedTime,
                ).let(::listOf)
            }
        }

        return emptyList()
    }

    private suspend fun getCategoryRecords(
        allRecords: List<RecordBase>
    ): Map<Long, List<RecordBase>> {
        val recordTypeCategories = recordTypeCategoryInteractor.getAll()
            .groupBy(RecordTypeCategory::categoryId)
            .mapValues { it.value.map(RecordTypeCategory::recordTypeId) }

        return recordTypeCategories
            .mapValues { (_, typeIds) -> allRecords.filter { it.typeIds.any { it in typeIds } } }
            .filterValues(List<RecordBase>::isNotEmpty)
    }

    private suspend fun getUncategorized(
        allRecords: List<RecordBase>
    ): List<RecordBase> {
        val recordTypeCategories = recordTypeCategoryInteractor.getAll().map { it.recordTypeId }

        return allRecords.filter { it.typeIds.all { it !in recordTypeCategories } }
    }
}