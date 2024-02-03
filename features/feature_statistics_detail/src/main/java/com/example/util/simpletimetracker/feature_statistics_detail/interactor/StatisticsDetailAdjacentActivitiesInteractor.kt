package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.getTypeIds
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsDetailAdjacentActivitiesInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordInteractor: RecordInteractor,
    private val timeMapper: TimeMapper,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
) {

    suspend fun getNextActivitiesViewData(
        filter: List<RecordsFilter>,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        // Show only if one activity is selected.
        val typeId = filter.getTypeIds()
            .takeIf { it.size == 1 }
            ?.firstOrNull()
            ?: return@withContext getEmptyViewData()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        // Don't use records from viewModel because they are already filtered,
        // we need all records here.
        val actualRecords = if (range.timeStarted == 0L && range.timeEnded == 0L) {
            recordInteractor.getAll()
        } else {
            recordInteractor.getFromRange(range)
        }
        val nextActivitiesIds = calculate(
            typeId = typeId,
            records = actualRecords,
        )

        return@withContext nextActivitiesIds.mapNotNull { type ->
            statisticsDetailViewDataMapper.mapToPreview(
                recordType = recordTypes[type] ?: return@mapNotNull null,
                isDarkTheme = isDarkTheme,
                isFirst = false,
                isForComparison = false,
            )
        }
    }

    private fun calculate(
        typeId: Long,
        records: List<RecordBase>,
    ): List<Long> {
        val counts = mutableMapOf<Long, Long>()

        val recordsSorted = records.sortedBy { it.timeStarted }
        var currentRecord: RecordBase? = null
        recordsSorted.forEach { record ->
            val currentTimeEnded = currentRecord?.timeEnded
            if (currentTimeEnded != null && record.timeStarted >= currentTimeEnded) {
                record.typeIds.firstOrNull()?.let { id ->
                    counts[id] = counts[id].orZero() + 1
                }
                currentRecord = null
            }
            if (currentRecord == null && typeId in record.typeIds) {
                currentRecord = record
            }
        }

        return counts.keys
            .sortedByDescending { counts[it].orZero() }
            .take(MAX_COUNT)
    }

    private fun getEmptyViewData(): List<ViewHolderType> {
        return emptyList()
    }

    companion object {
        private const val MAX_COUNT = 5
    }
}