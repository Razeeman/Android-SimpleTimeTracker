package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
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
                statisticsInteractor.getUntracked(range, records, addUntracked)
            )
            .plus(
                getUntagged(range, records, addUncategorized)
            )
    }

    private fun getUntagged(
        range: Range,
        records: List<Record>,
        addUncategorized: Boolean,
    ): List<Statistics> {
        if (addUncategorized) {
            val uncategorizedTime = statisticsInteractor.getDuration(
                range = range,
                records = getUntagged(records),
            )
            if (uncategorizedTime > 0L) {
                return Statistics(
                    id = UNCATEGORIZED_ITEM_ID,
                    duration = uncategorizedTime,
                ).let(::listOf)
            }
        }

        return emptyList()
    }

    private suspend fun getTagRecords(allRecords: List<Record>): Map<Long, List<Record>> {
        val recordTags = recordTagInteractor.getAll().map(RecordTag::id)

        return recordTags
            .associateWith { tagId -> allRecords.filter { tagId in it.tagIds } }
            .filterValues(List<Record>::isNotEmpty)
    }

    private fun getUntagged(allRecords: List<Record>): List<Record> {
        return allRecords.filter { it.tagIds.isEmpty() }
    }
}