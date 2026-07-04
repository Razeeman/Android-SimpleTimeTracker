package com.example.util.simpletimetracker.feature_records.interactor

import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.daysOfWeek.mapper.DaysInCalendarMapper
import com.example.util.simpletimetracker.domain.extension.dropSeconds
import com.example.util.simpletimetracker.domain.extension.toRange
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.record.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor.GetParam
import com.example.util.simpletimetracker.domain.record.interactor.RecordsContainerMultiselectInteractor
import com.example.util.simpletimetracker.domain.record.model.MultiSelectedRecordId
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordSelected.RecordSelectedViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecordSelected.RunningRecordSelectedViewData
import com.example.util.simpletimetracker.feature_records.customView.RecordsCalendarViewData
import com.example.util.simpletimetracker.feature_records.mapper.RecordsViewDataMapper
import com.example.util.simpletimetracker.feature_records.model.RecordsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Long.min
import java.util.Calendar
import java.util.Comparator
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max

class RecordsViewDataInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val getUntrackedRecordsInteractor: GetUntrackedRecordsInteractor,
    private val recordsViewDataMapper: RecordsViewDataMapper,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
    private val calendarToListShiftMapper: CalendarToListShiftMapper,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val daysInCalendarMapper: DaysInCalendarMapper,
    private val recordsContainerMultiselectInteractor: RecordsContainerMultiselectInteractor,
) {

    suspend fun getViewData(
        shift: Int,
        forSharing: Boolean,
    ): RecordsState = withContext(Dispatchers.Default) {
        val calendar = Calendar.getInstance()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val durationFormat = prefsInteractor.getDurationFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val isMilitary = prefsInteractor.getUseMilitaryTimeFormat()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val showUntrackedInRecords = prefsInteractor.getShowUntrackedInRecords()
        val reverseOrder = prefsInteractor.getReverseOrderInCalendar()
        val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val filterType = prefsInteractor.getListFilterType()
        val recordTags = recordTagInteractor.getAll()
        val goals = recordTypeGoalInteractor.getAllTypeGoals().groupBy { it.idData.value }
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypeCategories = suspend { recordTypeCategoryInteractor.getAll() }
        val filteredIds = prefsInteractor.getListFilteredIds(filterType)
        val isCalendarView = prefsInteractor.getShowRecordsCalendar()
        val daysInCalendar = if (isCalendarView) {
            prefsInteractor.getDaysInCalendar()
        } else {
            DaysInCalendar.ONE
        }
        val daysCountInShift = daysInCalendarMapper.mapDaysCount(daysInCalendar)
        val multiSelectedIds = recordsContainerMultiselectInteractor.selectedRecordIds

        return@withContext (daysCountInShift - 1 downTo 0).map { dayInShift ->
            val actualShift = calendarToListShiftMapper.mapCalendarToListShift(
                calendarShift = shift,
                daysInCalendar = daysInCalendar,
                startOfDayShift = startOfDayShift,
                firstDayOfWeek = firstDayOfWeek,
            ).end - dayInShift

            val range = timeMapper.getRangeStartAndEnd(
                rangeLength = RangeLength.Day,
                shift = actualShift,
                firstDayOfWeek = DayOfWeek.MONDAY, // Doesn't matter for days.
                startOfDayShift = startOfDayShift,
            )
            val records = recordInteractor.getWithParams(GetParam.FromRange(range))

            val data = getRecordsViewData(
                records = records,
                runningRecords = runningRecords,
                filterType = filterType,
                filteredIds = filteredIds,
                recordTypes = recordTypes,
                recordTags = recordTags,
                goals = goals,
                recordTypeCategories = recordTypeCategories,
                range = range,
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                durationFormat = durationFormat,
                showUntrackedInRecords = showUntrackedInRecords,
                showSeconds = showSeconds,
            )

            ViewDataIntermediate(
                rangeStart = range.timeStarted,
                rangeEnd = range.timeEnded,
                isToday = actualShift == 0,
                records = data,
            )
        }.let { data ->
            if (isCalendarView) {
                mapCalendarData(
                    data = data,
                    calendar = calendar,
                    startOfDayShift = startOfDayShift,
                    shift = shift,
                    reverseOrder = reverseOrder,
                    showSeconds = showSeconds,
                    isMilitary = isMilitary,
                    multiSelectedIds = multiSelectedIds,
                )
            } else {
                mapRecordsData(
                    data = data,
                    shift = shift,
                    forSharing = forSharing,
                    multiSelectedIds = multiSelectedIds,
                )
            }
        }
    }

    private fun mapCalendarData(
        data: List<ViewDataIntermediate>,
        calendar: Calendar,
        startOfDayShift: Long,
        shift: Int,
        reverseOrder: Boolean,
        showSeconds: Boolean,
        isMilitary: Boolean,
        multiSelectedIds: List<MultiSelectedRecordId>,
    ): RecordsState.CalendarData.Data {
        val currentTime = if (shift == 0) {
            timeMapper.mapFromStartOfDay(
                timeStamp = System.currentTimeMillis(),
                calendar = calendar,
            ) - startOfDayShift
        } else {
            null
        }
        val shouldMapLegends = data.size > 1

        return data
            .map { column ->
                val legend = if (shouldMapLegends) {
                    timeMapper.getDayOfWeek(
                        timestamp = column.rangeStart,
                        calendar = calendar,
                        startOfDayShift = startOfDayShift,
                    ).let(timeMapper::toShortDayOfWeekName)
                } else {
                    ""
                }

                val points = column.records.map { record ->
                    mapToCalendarPoint(
                        holder = record,
                        calendar = calendar,
                        startOfDayShift = startOfDayShift,
                        rangeStart = column.rangeStart,
                        rangeEnd = column.rangeEnd,
                        showSeconds = showSeconds,
                        multiSelectedIds = multiSelectedIds,
                    )
                }

                RecordsCalendarViewData.Points(
                    legend = legend,
                    highlighted = column.isToday,
                    data = points,
                )
            }
            .let { list ->
                RecordsCalendarViewData(
                    currentTime = currentTime,
                    startOfDayShift = startOfDayShift,
                    points = list,
                    reverseOrder = reverseOrder,
                    shouldDrawTopLegends = shouldMapLegends,
                    isMilitary = isMilitary,
                )
            }
            .let(RecordsState.CalendarData::Data)
    }

    private suspend fun mapRecordsData(
        data: List<ViewDataIntermediate>,
        shift: Int,
        forSharing: Boolean,
        multiSelectedIds: List<MultiSelectedRecordId>,
    ): RecordsState.RecordsData {
        val records = data.firstOrNull()?.records.orEmpty()

        val showFirstEnterHint = when {
            // Show hint only on current date.
            shift != 0 -> false
            // Check all records only if there is no records for this day.
            records.isNotEmpty() -> false
            // Try to find if any record exists.
            else -> recordInteractor.isEmpty() && runningRecordInteractor.isEmpty()
        }

        val hint = if (!forSharing) {
            recordsViewDataMapper.mapToHint()
        } else {
            null
        }

        val sortComparator: Comparator<RecordHolder> = compareByDescending<RecordHolder> {
            it.timeStartedTimestamp
        }.thenBy {
            // Otherwise 0 duration activities would be on top of untracked.
            it.data.typeId != UNTRACKED_ITEM_ID
        }

        val items = when {
            showFirstEnterHint -> listOf(recordViewDataMapper.mapToNoRecords())
            records.isEmpty() -> listOf(recordViewDataMapper.mapToEmpty())
            else -> {
                records
                    .sortedWith(sortComparator)
                    .map { remapForMultiselect(it, multiSelectedIds) } +
                    listOfNotNull(hint)
            }
        }

        return RecordsState.RecordsData(items)
    }

    private fun remapForMultiselect(
        holder: RecordHolder,
        multiSelectedIds: List<MultiSelectedRecordId>,
    ): ViewHolderType {
        // If disabled - return right away.
        if (multiSelectedIds.isEmpty()) {
            return holder.data.value
        }
        val multiSelectedId = mapMultiSelectedId(holder)
        return when (holder.data) {
            is RecordHolder.Data.RecordData -> {
                val value = holder.data.value
                if (multiSelectedId in multiSelectedIds) {
                    RecordSelectedViewData(value)
                } else {
                    value
                }
            }
            is RecordHolder.Data.RunningRecordData -> {
                val value = holder.data.value
                if (multiSelectedId in multiSelectedIds) {
                    RunningRecordSelectedViewData(value)
                } else {
                    value
                }
            }
        }
    }

    private suspend fun getRecordsViewData(
        records: List<Record>,
        runningRecords: List<RunningRecord>,
        filterType: ChartFilterType,
        filteredIds: List<Long>,
        recordTypes: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
        goals: Map<Long, List<RecordTypeGoal>>,
        recordTypeCategories: suspend () -> List<RecordTypeCategory>,
        range: Range,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        durationFormat: DurationFormat,
        showUntrackedInRecords: Boolean,
        showSeconds: Boolean,
    ): List<RecordHolder> {
        val trackedRecordsData = records
            .map { record ->
                recordsViewDataMapper.map(
                    record = record,
                    recordType = recordTypes[record.typeId],
                    recordTags = recordTags,
                    range = range,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    durationFormat = durationFormat,
                    showSeconds = showSeconds,
                ).let {
                    RecordHolder(
                        timeStartedTimestamp = it.timeStartedTimestamp,
                        data = RecordHolder.Data.RecordData(
                            value = it,
                            typeId = record.typeId,
                            tagIds = record.tags.map(RecordBase.Tag::tagId),
                        ),
                    )
                }
            }

        val runningRecordsData = runningRecords
            .let {
                rangeMapper.getRunningRecordsFromRange(it, range)
            }
            .mapNotNull { runningRecord ->
                getRunningRecordViewDataMediator.execute(
                    type = recordTypes[runningRecord.id] ?: return@mapNotNull null,
                    tags = recordTags,
                    goals = goals[runningRecord.id].orEmpty(),
                    record = runningRecord,
                    nowIconVisible = true,
                    goalsVisible = false,
                    totalDurationVisible = false,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    durationFormat = durationFormat,
                    showSeconds = showSeconds,
                ).let {
                    RecordHolder(
                        timeStartedTimestamp = runningRecord.timeStarted,
                        data = RecordHolder.Data.RunningRecordData(
                            value = it,
                            typeId = runningRecord.id,
                            tagIds = runningRecord.tags.map(RecordBase.Tag::tagId),
                        ),
                    )
                }
            }

        val untrackedRecordsData = if (
            showUntrackedInRecords &&
            UNTRACKED_ITEM_ID !in filteredIds
        ) {
            val recordRanges = records.map(Record::toRange)
            val runningRecordRanges = runningRecords.map(RunningRecord::toRange)
            getUntrackedRecordsInteractor.get(
                range = range,
                records = recordRanges + runningRecordRanges,
            ).map { untrackedRecord ->
                recordsViewDataMapper.mapToUntracked(
                    record = untrackedRecord,
                    range = range,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    durationFormat = durationFormat,
                    showSeconds = showSeconds,
                ).let {
                    RecordHolder(
                        timeStartedTimestamp = it.timeStartedTimestamp,
                        data = RecordHolder.Data.RecordData(
                            value = it,
                            typeId = UNTRACKED_ITEM_ID,
                            tagIds = emptyList(),
                        ),
                    )
                }
            }
        } else {
            emptyList()
        }

        return filterRecords(
            records = runningRecordsData + trackedRecordsData, // Timers should be first for correct sort.
            chartFilterType = filterType,
            filteredIds = filteredIds,
            lazyRecordTypeCategories = recordTypeCategories,
        ) + untrackedRecordsData
    }

    private suspend fun filterRecords(
        records: List<RecordHolder>,
        chartFilterType: ChartFilterType,
        filteredIds: List<Long>,
        lazyRecordTypeCategories: suspend () -> List<RecordTypeCategory>,
    ): List<RecordHolder> {
        return when (chartFilterType) {
            ChartFilterType.ACTIVITY -> {
                records.filter {
                    it.data.typeId !in filteredIds
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
                records.filter {
                    val typeId = it.data.typeId
                    if (typeId !in categorizedTypeIds) {
                        UNCATEGORIZED_ITEM_ID !in filteredIds
                    } else {
                        typeId !in filteredTypeIds
                    }
                }
            }
            ChartFilterType.RECORD_TAG -> {
                records.filter {
                    val tagIds = it.data.tagIds
                    if (tagIds.isEmpty()) {
                        UNCATEGORIZED_ITEM_ID !in filteredIds
                    } else {
                        tagIds.all { tagId -> tagId !in filteredIds }
                    }
                }
            }
        }
    }

    private fun mapToCalendarPoint(
        holder: RecordHolder,
        calendar: Calendar,
        startOfDayShift: Long,
        rangeStart: Long,
        rangeEnd: Long,
        showSeconds: Boolean,
        multiSelectedIds: List<MultiSelectedRecordId>,
    ): RecordsCalendarViewData.Point {
        // Record data already clamped.
        val timeStartedTimestamp = when (holder.data) {
            is RecordHolder.Data.RecordData ->
                holder.timeStartedTimestamp.let { if (showSeconds) it else it.dropSeconds() }
            is RecordHolder.Data.RunningRecordData ->
                max(holder.timeStartedTimestamp, rangeStart)
        }
        val timeEndedTimestamp = when (holder.data) {
            is RecordHolder.Data.RecordData ->
                holder.data.value.timeEndedTimestamp.let { if (showSeconds) it else it.dropSeconds() }
            is RecordHolder.Data.RunningRecordData ->
                min(System.currentTimeMillis(), rangeEnd)
        }

        val start = timeMapper.mapFromStartOfDay(
            // Normalize to set start of day correctly.
            timeStamp = timeStartedTimestamp - startOfDayShift,
            calendar = calendar,
        ) + startOfDayShift

        val duration = (timeEndedTimestamp - timeStartedTimestamp)
            // Otherwise would be invisible.
            .takeUnless { it == 0L } ?: minuteInMillis

        val end = start + duration

        val isSelected = mapMultiSelectedId(holder) in multiSelectedIds

        return RecordsCalendarViewData.Point(
            start = start - startOfDayShift,
            end = end - startOfDayShift,
            isSelected = isSelected,
            data = when (holder.data) {
                is RecordHolder.Data.RecordData -> {
                    RecordsCalendarViewData.Point.Data.RecordData(holder.data.value)
                }
                is RecordHolder.Data.RunningRecordData -> {
                    RecordsCalendarViewData.Point.Data.RunningRecordData(holder.data.value)
                }
            },
        )
    }

    private fun mapMultiSelectedId(
        holder: RecordHolder,
    ): MultiSelectedRecordId {
        return when (holder.data) {
            is RecordHolder.Data.RecordData -> {
                when (val value = holder.data.value) {
                    is RecordViewData.Tracked -> MultiSelectedRecordId.Tracked(value.id)
                    is RecordViewData.Untracked -> MultiSelectedRecordId.Untracked(
                        timeStartedTimestamp = value.timeStartedTimestamp,
                        timeEndedTimestamp = value.timeEndedTimestamp,
                    )
                }
            }
            is RecordHolder.Data.RunningRecordData -> {
                val value = holder.data.value
                MultiSelectedRecordId.Running(value.id)
            }
        }
    }

    private data class RecordHolder(
        val timeStartedTimestamp: Long,
        val data: Data,
    ) {
        sealed interface Data {
            val value: ViewHolderType
            val typeId: Long
            val tagIds: List<Long>

            data class RecordData(
                override val value: RecordViewData,
                override val typeId: Long,
                override val tagIds: List<Long>,
            ) : Data

            data class RunningRecordData(
                override val value: RunningRecordViewData,
                override val typeId: Long,
                override val tagIds: List<Long>,
            ) : Data
        }
    }

    private data class ViewDataIntermediate(
        val rangeStart: Long,
        val rangeEnd: Long,
        val isToday: Boolean,
        val records: List<RecordHolder>,
    )

    companion object {
        private val minuteInMillis = TimeUnit.MINUTES.toMillis(1)
    }
}