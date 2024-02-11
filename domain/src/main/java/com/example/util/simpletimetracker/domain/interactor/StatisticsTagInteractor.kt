package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.Statistics
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatisticsTagInteractor @Inject constructor(
    private val recordTagInteractor: RecordTagInteractor,
    private val statisticsInteractor: StatisticsInteractor,
) {

    suspend fun getFromRange(
        range: Range,
        addUntracked: Boolean,
        addUncategorized: Boolean,
    ): List<Statistics> = withContext(Dispatchers.IO) {
        val records = statisticsInteractor.getRecords(range)

        getTagRecords(records)
            .let {
                statisticsInteractor.getStatistics(range, it)
            }
            .plus(
                statisticsInteractor.getUntracked(range, records, addUntracked),
            )
            .plus(
                getUntagged(range, records, addUncategorized),
            )
    }

    private fun getUntagged(
        range: Range,
        records: List<RecordBase>,
        addUncategorized: Boolean,
    ): List<Statistics> {
        if (addUncategorized) {
            val uncategorizedTime = statisticsInteractor.getStatisticsData(
                range = range,
                records = getUntagged(records),
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

    private suspend fun getTagRecords(
        allRecords: List<RecordBase>,
    ): Map<Long, List<RecordBase>> {
        val recordTags = recordTagInteractor.getAll().map(RecordTag::id)

        return recordTags
            .associateWith { tagId -> allRecords.filter { tagId in it.tagIds } }
            .filterValues(List<RecordBase>::isNotEmpty)
    }

    private fun getUntagged(
        allRecords: List<RecordBase>,
    ): List<RecordBase> {
        return allRecords.filter { it.tagIds.isEmpty() }
    }
}