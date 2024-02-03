package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
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
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsDetailAdjacentActivitiesInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
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
        val typeId = getActivityId(filter) ?: return@withContext getEmptyViewData()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val actualRecords = getRecords(rangeLength, rangePosition)
        val nextActivitiesIds = calculateNextActivities(typeId, actualRecords)
        val multitaskingActivitiesIds = calculateMultitasking(typeId, actualRecords)

        fun mapPreviews(typeToCounts: List<CalculationResult>): List<ViewHolderType> {
            val total = typeToCounts.sumOf(CalculationResult::count)
                .takeUnless { it == 0L }
                ?: return emptyList()

            val typeToPercents = typeToCounts.map { result ->
                result.typeId to (result.count * 100 / total)
            }

            return typeToPercents.mapIndexedNotNull { index, (typeId, percent) ->
                // So that all items would sum up to 100,
                // adjust value of the first element according to all the rest elements.
                val isFirst = index == 0
                val correctedPercent = if (isFirst) {
                    100 - typeToPercents.drop(1).sumOf { it.second }
                } else {
                    percent
                }

                statisticsDetailViewDataMapper.mapToPreview(
                    recordType = recordTypes[typeId] ?: return@mapIndexedNotNull null,
                    isDarkTheme = isDarkTheme,
                    isFirst = false,
                    isForComparison = false,
                ).copy(name = "$correctedPercent%")
            }
        }

        val nextActivities = nextActivitiesIds
            .let(::mapPreviews)
        val nextActivitiesHint = resourceRepo
            .getString(R.string.statistics_detail_next_activities_hint)
            .let(::HintViewData)
            .takeIf { nextActivities.isNotEmpty() }
            .let(::listOfNotNull)

        val multitaskingActivities = multitaskingActivitiesIds
            .let(::mapPreviews)
        val multitaskingActivitiesHint = resourceRepo
            .getString(R.string.statistics_detail_multitasking_activities_hint)
            .let(::HintViewData)
            .takeIf { multitaskingActivities.isNotEmpty() }
            .let(::listOfNotNull)

        return@withContext nextActivitiesHint +
            nextActivities +
            multitaskingActivitiesHint +
            multitaskingActivities
    }

    // Don't use records from viewModel because they are already filtered,
    // we need all records here.
    private suspend fun getRecords(
        rangeLength: RangeLength,
        rangePosition: Int,
    ): List<RecordBase> {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        return if (range.timeStarted == 0L && range.timeEnded == 0L) {
            recordInteractor.getAll()
        } else {
            recordInteractor.getFromRange(range)
        }
    }

    private fun getActivityId(
        filter: List<RecordsFilter>,
    ): Long? {
        return filter.getTypeIds()
            .takeIf { it.size == 1 }
            ?.firstOrNull()
    }

    // TODO make more precise calculations?
    private fun calculateNextActivities(
        typeId: Long,
        records: List<RecordBase>,
    ): List<CalculationResult> {
        val counts = mutableMapOf<Long, Long>()

        val recordsSorted = records.sortedBy { it.timeStarted }
        var currentRecord: RecordBase? = null
        recordsSorted.forEach { record ->
            val currentTimeEnded = currentRecord?.timeEnded
            if (currentTimeEnded != null &&
                currentTimeEnded <= record.timeStarted
            ) {
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
            .map { CalculationResult(it, counts[it].orZero()) }
    }

    // TODO make more precise calculations?
    private fun calculateMultitasking(
        typeId: Long,
        records: List<RecordBase>,
    ): List<CalculationResult> {
        val counts = mutableMapOf<Long, Long>()

        val recordsSorted = records.sortedBy { it.timeStarted }
        var currentRecord: RecordBase? = null
        recordsSorted.forEach { record ->
            val currentTimeStarted = currentRecord?.timeStarted
            val currentTimeEnded = currentRecord?.timeEnded
            if (currentTimeStarted != null &&
                currentTimeEnded != null &&
                // Find next records that was started after this one but before this one ends.
                currentTimeStarted <= record.timeStarted &&
                currentTimeEnded > record.timeStarted &&
                // Cutoff short intersections.
                currentTimeEnded - record.timeStarted > 1_000L
            ) {
                record.typeIds.firstOrNull()?.let { id ->
                    counts[id] = counts[id].orZero() + 1
                }
            }
            if (typeId in record.typeIds) {
                currentRecord = record
            }
        }

        return counts.keys
            .sortedByDescending { counts[it].orZero() }
            .take(MAX_COUNT)
            .map { CalculationResult(it, counts[it].orZero()) }
    }

    private fun getEmptyViewData(): List<ViewHolderType> {
        return emptyList()
    }

    private data class CalculationResult(
        val typeId: Long,
        val count: Long,
    )

    companion object {
        private const val MAX_COUNT = 5
    }
}