package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.Statistics
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatisticsTagInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsMapper: StatisticsMapper
) {

    suspend fun getAll(): List<Statistics> = withContext(Dispatchers.IO) {
        val allRecords = recordInteractor.getAll() // TODO expensive, get by filter

        getTagRecords(allRecords)
            .map { (tagId, records) ->
                Statistics(
                    id = tagId,
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

        getTagRecords(allRecords)
            .map { (tagId, records) ->
                Statistics(
                    id = tagId,
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
                    val untaggedTime = getUntagged(allRecords)
                        .let(statisticsMapper::mapToDuration) // TODO from range (remove parts not in range).
                    if (untaggedTime > 0L) {
                        this as MutableList
                        Statistics(
                            id = UNCATEGORIZED_ITEM_ID,
                            duration = untaggedTime,
                        ).let(::add)
                    }
                }
            }
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