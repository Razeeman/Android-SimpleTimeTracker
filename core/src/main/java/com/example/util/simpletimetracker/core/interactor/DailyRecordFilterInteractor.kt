package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import java.util.Comparator
import javax.inject.Inject

/** Shared exclusion rules used by every daily records list. */
class DailyRecordFilterInteractor @Inject constructor() {

    suspend fun <T> filter(
        runningRecordsData: List<RecordHolder<T>>,
        trackedRecordsData: List<RecordHolder<T>>,
        untrackedRecordsData: List<RecordHolder<T>>,
        chartFilterType: ChartFilterType,
        filteredIds: Collection<Long>,
        lazyRecordTypeCategories: suspend () -> List<RecordTypeCategory>,
    ): List<RecordHolder<T>> {
        return filter(
            records = runningRecordsData + trackedRecordsData, // Timers should be first for correct sort.
            chartFilterType = chartFilterType,
            filteredIds = filteredIds,
            lazyRecordTypeCategories = lazyRecordTypeCategories,
        ) + untrackedRecordsData
    }

    fun <T> sort(
        data: List<RecordHolder<T>>,
    ): List<RecordHolder<T>> {
        val sortComparator: Comparator<RecordHolder<T>> = compareByDescending<RecordHolder<T>> {
            it.timeStartedTimestamp
        }.thenBy {
            // Otherwise 0 duration activities would be on top of untracked.
            it.typeId != UNTRACKED_ITEM_ID
        }

        return data.sortedWith(sortComparator)
    }

    private suspend fun <T> filter(
        records: List<RecordHolder<T>>,
        chartFilterType: ChartFilterType,
        filteredIds: Collection<Long>,
        lazyRecordTypeCategories: suspend () -> List<RecordTypeCategory>,
    ): List<RecordHolder<T>> {
        if (filteredIds.isEmpty()) return records

        return when (chartFilterType) {
            ChartFilterType.ACTIVITY -> {
                records.filter { record ->
                    record.typeId !in filteredIds
                }
            }
            ChartFilterType.CATEGORY -> {
                val recordTypeCategories = lazyRecordTypeCategories.invoke()
                val categorizedTypeIds = recordTypeCategories
                    .map(RecordTypeCategory::recordTypeId)
                    .distinct()
                val filteredTypeIds = recordTypeCategories
                    .filter { it.categoryId in filteredIds }
                    .map(RecordTypeCategory::recordTypeId)
                    .distinct()
                records.filter { record ->
                    val recordTypeId = record.typeId
                    if (recordTypeId !in categorizedTypeIds) {
                        UNCATEGORIZED_ITEM_ID !in filteredIds
                    } else {
                        recordTypeId !in filteredTypeIds
                    }
                }
            }
            ChartFilterType.RECORD_TAG -> {
                records.filter { record ->
                    val recordTagIds = record.tagIds
                    if (recordTagIds.isEmpty()) {
                        UNCATEGORIZED_ITEM_ID !in filteredIds
                    } else {
                        recordTagIds.none { it in filteredIds }
                    }
                }
            }
        }
    }

    data class RecordHolder<T>(
        val timeStartedTimestamp: Long,
        val typeId: Long,
        val tagIds: List<Long>,
        val data: T,
    )
}
