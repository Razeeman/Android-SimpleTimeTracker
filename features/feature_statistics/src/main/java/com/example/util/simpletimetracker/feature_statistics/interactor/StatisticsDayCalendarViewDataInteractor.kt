package com.example.util.simpletimetracker.feature_statistics.interactor

import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.view.dayCalendar.DayCalendarViewData
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.toRange
import com.example.util.simpletimetracker.domain.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsTagInteractor
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsDayCalendarViewData
import java.util.Calendar
import javax.inject.Inject

class StatisticsDayCalendarViewDataInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val colorMapper: ColorMapper,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val statisticsTagInteractor: StatisticsTagInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val getUntrackedRecordsInteractor: GetUntrackedRecordsInteractor,
) {

    suspend fun getViewData(
        filterType: ChartFilterType,
        filteredIds: List<Long>,
        range: Range,
        rangeLength: RangeLength,
    ): StatisticsDayCalendarViewData? {
        if (rangeLength != RangeLength.Day) return null
        if (!prefsInteractor.getShowDailyCalendar()) return null

        val types = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val showUntracked = prefsInteractor.getShowUntrackedInStatistics()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = types,
        )
        val data = getData(
            filterType = filterType,
            range = range,
            addUntracked = !filteredIds.contains(UNTRACKED_ITEM_ID) && showUntracked,
            addUncategorized = !filteredIds.contains(UNCATEGORIZED_ITEM_ID),
        ).filter {
            it.id !in filteredIds
        }.map {
            mapToCalendarPoint(
                holder = it,
                dataHolders = dataHolders,
                calendar = Calendar.getInstance(),
                startOfDayShift = startOfDayShift,
                isDarkTheme = isDarkTheme,
            )
        }

        return StatisticsDayCalendarViewData(
            data = DayCalendarViewData(data),
        )
    }

    private suspend fun getData(
        filterType: ChartFilterType,
        range: Range,
        addUntracked: Boolean,
        addUncategorized: Boolean,
    ): List<RecordHolder> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> getActivityData(
                range = range,
                addUntracked = addUntracked,
            )

            ChartFilterType.CATEGORY -> getCategoryData(
                range = range,
                addUntracked = addUntracked,
                addUncategorized = addUncategorized,
            )

            ChartFilterType.RECORD_TAG -> getTagData(
                range = range,
                addUntracked = addUntracked,
                addUncategorized = addUncategorized,
            )
        }
    }

    private suspend fun getActivityData(
        range: Range,
        addUntracked: Boolean,
    ): List<RecordHolder> {
        val records = statisticsInteractor.getRecords(range)

        return rangeMapper.getRecordsFromRange(records, range)
            .map {
                mapRecord(
                    // Multitask is not available in statistics.
                    id = it.typeIds.firstOrNull().orZero(),
                    recordBase = it,
                    range = range,
                )
            }
            .plus(getUntracked(range, records, addUntracked))
    }

    private suspend fun getCategoryData(
        range: Range,
        addUntracked: Boolean,
        addUncategorized: Boolean,
    ): List<RecordHolder> {
        val records = statisticsInteractor.getRecords(range)

        return statisticsCategoryInteractor.getCategoryRecords(records)
            .flatMap { (categoryId, records) ->
                records.map {
                    mapRecord(
                        id = categoryId,
                        recordBase = it,
                        range = range,
                    )
                }
            }
            .plus(getUntracked(range, records, addUntracked))
            .plus(getUncategorized(range, records, addUncategorized))
    }

    private suspend fun getTagData(
        range: Range,
        addUntracked: Boolean,
        addUncategorized: Boolean,
    ): List<RecordHolder> {
        val records = statisticsInteractor.getRecords(range)

        return statisticsTagInteractor.getTagRecords(records)
            .flatMap { (categoryId, records) ->
                records.map {
                    mapRecord(
                        id = categoryId,
                        recordBase = it,
                        range = range,
                    )
                }
            }
            .plus(getUntracked(range, records, addUntracked))
            .plus(getUntagged(range, records, addUncategorized))
    }

    private suspend fun getUntracked(
        range: Range,
        records: List<RecordBase>,
        addUntracked: Boolean,
    ): List<RecordHolder> {
        if (!addUntracked) return emptyList()

        return getUntrackedRecordsInteractor.get(
            range = range,
            records = records.map(RecordBase::toRange),
        ).map {
            mapRecord(
                id = UNTRACKED_ITEM_ID,
                recordBase = it,
                range = range,
            )
        }
    }

    private suspend fun getUncategorized(
        range: Range,
        records: List<RecordBase>,
        addUncategorized: Boolean,
    ): List<RecordHolder> {
        if (!addUncategorized) return emptyList()

        return statisticsCategoryInteractor.getUncategorized(
            allRecords = records,
        ).map {
            mapRecord(
                id = UNCATEGORIZED_ITEM_ID,
                recordBase = it,
                range = range,
            )
        }
    }

    private fun getUntagged(
        range: Range,
        records: List<RecordBase>,
        addUncategorized: Boolean,
    ): List<RecordHolder> {
        if (!addUncategorized) return emptyList()
        return statisticsTagInteractor.getUntagged(
            records,
        ).map {
            mapRecord(
                id = UNCATEGORIZED_ITEM_ID,
                recordBase = it,
                range = range,
            )
        }
    }

    private fun mapToCalendarPoint(
        holder: RecordHolder,
        dataHolders: Map<Long, StatisticsDataHolder>,
        calendar: Calendar,
        startOfDayShift: Long,
        isDarkTheme: Boolean,
    ): DayCalendarViewData.Point {
        val start = timeMapper.mapFromStartOfDay(
            // Normalize to set start of day correctly.
            timeStamp = holder.timeStartedTimestamp - startOfDayShift,
            calendar = calendar,
        ) + startOfDayShift
        val duration = holder.timeEndedTimestamp - holder.timeStartedTimestamp
        val end = start + duration
        val color = mapColor(
            holder = holder,
            dataHolder = dataHolders[holder.id],
            isDarkTheme = isDarkTheme,
        )

        return DayCalendarViewData.Point(
            start = start - startOfDayShift,
            end = end - startOfDayShift,
            data = DayCalendarViewData.Point.Data(
                color = color,
            ),
        )
    }

    private fun mapRecord(
        id: Long,
        recordBase: RecordBase,
        range: Range,
    ): RecordHolder {
        val thisRange = rangeMapper.clampToRange(recordBase, range)
        return RecordHolder(
            id = id,
            timeStartedTimestamp = thisRange.timeStarted,
            timeEndedTimestamp = thisRange.timeEnded,
        )
    }

    private fun mapColor(
        holder: RecordHolder,
        dataHolder: StatisticsDataHolder?,
        isDarkTheme: Boolean,
    ): Int {
        return when {
            holder.id == UNTRACKED_ITEM_ID -> {
                colorMapper.toUntrackedColor(isDarkTheme)
            }
            holder.id == UNCATEGORIZED_ITEM_ID -> {
                colorMapper.toUntrackedColor(isDarkTheme)
            }
            dataHolder != null -> {
                colorMapper.mapToColorInt(dataHolder.color, isDarkTheme)
            }
            else -> {
                colorMapper.toUntrackedColor(isDarkTheme)
            }
        }
    }

    private data class RecordHolder(
        val id: Long,
        val timeStartedTimestamp: Long,
        val timeEndedTimestamp: Long,
    )
}