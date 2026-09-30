package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.interactor.DailyRecordFilterInteractor.RecordHolder
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.mapper.WearDataLocalMapper
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.toRange
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.wear_api.WearChartFilterTypeDTO
import com.example.util.simpletimetracker.wear_api.WearRecordDTO
import com.example.util.simpletimetracker.wear_api.WearStatisticsDTO
import javax.inject.Inject

/**
 * Records and statistics of a single day in wear api format.
 * Shared by wear and external broadcast queries, so it should not depend on any flavor specific code.
 */
class WearQueryInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val wearDataLocalMapper: WearDataLocalMapper,
    private val statisticsMediator: StatisticsMediator,
    private val getUntrackedRecordsInteractor: GetUntrackedRecordsInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val dailyRecordFilterInteractor: DailyRecordFilterInteractor,
) {

    suspend fun queryStatistics(
        shift: Int,
        filterTypeDto: WearChartFilterTypeDTO,
    ): List<WearStatisticsDTO> {
        val filterType = wearDataLocalMapper.map(filterTypeDto)
        val rangeLength = RangeLength.Day
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val types = recordTypeInteractor.getAll().associateBy(RecordType::id)

        val filteredIds = when (filterType) {
            ChartFilterType.ACTIVITY -> prefsInteractor.getFilteredTypes()
            ChartFilterType.CATEGORY -> prefsInteractor.getFilteredCategories()
            ChartFilterType.RECORD_TAG -> prefsInteractor.getFilteredTags()
        }

        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = types,
        )
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = shift,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val statistics = statisticsMediator.getStatistics(
            filterType = filterType,
            filteredIds = filteredIds,
            range = range,
            forceSeconds = false,
        ).filterNot {
            it.id in filteredIds
        }

        return statistics.map {
            wearDataLocalMapper.map(
                statistics = it,
                dataHolder = dataHolders[it.id],
                filterType = filterType,
            )
        }
    }

    suspend fun queryRecords(shift: Int): List<WearRecordDTO> {
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = RangeLength.Day,
            shift = shift,
            firstDayOfWeek = DayOfWeek.MONDAY, // Doesn't matter for days.
            startOfDayShift = startOfDayShift,
        )
        val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val recordTags = recordTagInteractor.getAll()
        val filterType = prefsInteractor.getListFilterType()
        val filteredIds = prefsInteractor.getListFilteredIds(filterType)
        val categories = suspend { recordTypeCategoryInteractor.getAll() }
        val records = recordInteractor.getWithParams(RecordInteractor.GetParam.FromRange(range))
        val runningRecords = runningRecordInteractor.getAll()

        val trackedRecordsData = records.map { record ->
            wearDataLocalMapper.mapRecord(
                id = record.id,
                type = WearRecordDTO.TypeDTO.TRACKED,
                recordType = recordTypes[record.typeId],
                record = record,
                tags = recordTags,
                range = range,
            ).let {
                RecordHolder(
                    timeStartedTimestamp = it.startedAt,
                    typeId = record.typeId,
                    tagIds = record.tags.map(RecordBase.Tag::tagId),
                    data = Data(it),
                )
            }
        }

        val runningRecordsData = runningRecords.let {
            rangeMapper.getRunningRecordsFromRange(it, range)
        }.map { runningRecord ->
            wearDataLocalMapper.mapRecord(
                id = runningRecord.id,
                type = WearRecordDTO.TypeDTO.RUNNING,
                recordType = recordTypes[runningRecord.id],
                record = runningRecord,
                tags = recordTags,
                range = range,
            ).let {
                RecordHolder(
                    timeStartedTimestamp = it.startedAt,
                    typeId = runningRecord.id,
                    tagIds = runningRecord.tags.map(RecordBase.Tag::tagId),
                    data = Data(it),
                )
            }
        }

        val untrackedRecordsData = if (
            prefsInteractor.getShowUntrackedInRecords() &&
            UNTRACKED_ITEM_ID !in filteredIds
        ) {
            val recordRanges = records.map(Record::toRange)
            val runningRecordRanges = runningRecords.map(RunningRecord::toRange)
            getUntrackedRecordsInteractor.get(
                range = range,
                records = recordRanges + runningRecordRanges,
            ).map { untrackedRecord ->
                wearDataLocalMapper.mapRecord(
                    id = untrackedRecord.timeStarted,
                    type = WearRecordDTO.TypeDTO.UNTRACKED,
                    recordType = null,
                    record = untrackedRecord,
                    tags = recordTags,
                    range = range,
                ).let {
                    RecordHolder(
                        timeStartedTimestamp = it.startedAt,
                        typeId = UNTRACKED_ITEM_ID,
                        tagIds = emptyList(),
                        data = Data(it),
                    )
                }
            }
        } else {
            emptyList()
        }

        val result = dailyRecordFilterInteractor.filter(
            runningRecordsData = runningRecordsData,
            trackedRecordsData = trackedRecordsData,
            untrackedRecordsData = untrackedRecordsData,
            chartFilterType = filterType,
            filteredIds = filteredIds,
            lazyRecordTypeCategories = categories,
        )

        return dailyRecordFilterInteractor.sort(result).map { it.data.value }
    }

    private data class Data(
        val value: WearRecordDTO,
    )
}
