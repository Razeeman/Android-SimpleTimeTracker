package com.example.util.simpletimetracker.feature_records.interactor

import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.interactor.DailyRecordFilterInteractor
import com.example.util.simpletimetracker.core.interactor.DailyRecordFilterInteractor.RecordHolder
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.base.DurationFormat
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
    private val dailyRecordFilterInteractor: DailyRecordFilterInteractor,
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

        val items = when {
            showFirstEnterHint -> listOf(recordViewDataMapper.mapToNoRecords())
            records.isEmpty() -> listOf(recordViewDataMapper.mapToEmpty())
            else -> {
                dailyRecordFilterInteractor.sort(records)
                    .map { remapForMultiselect(it, multiSelectedIds) } +
                    listOfNotNull(hint)
            }
        }

        return RecordsState.RecordsData(items)
    }

    private fun remapForMultiselect(
        holder: RecordHolder<Data>,
        multiSelectedIds: List<MultiSelectedRecordId>,
    ): ViewHolderType {
        // If disabled - return right away.
        if (multiSelectedIds.isEmpty()) {
            return holder.data.value
        }
        val multiSelectedId = mapMultiSelectedId(holder)
        return when (val data = holder.data) {
            is Data.RecordData -> {
                val value = data.value
                if (multiSelectedId in multiSelectedIds) {
                    RecordSelectedViewData(value)
                } else {
                    value
                }
            }
            is Data.RunningRecordData -> {
                val value = data.value
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
    ): List<RecordHolder<Data>> {
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
                    RecordHolder<Data>(
                        timeStartedTimestamp = it.timeStartedTimestamp,
                        typeId = record.typeId,
                        tagIds = record.tags.map(RecordBase.Tag::tagId),
                        data = Data.RecordData(it),
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
                    RecordHolder<Data>(
                        timeStartedTimestamp = it.timeStartedTimestamp,
                        typeId = runningRecord.id,
                        tagIds = runningRecord.tags.map(RecordBase.Tag::tagId),
                        data = Data.RunningRecordData(it),
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
                    RecordHolder<Data>(
                        timeStartedTimestamp = it.timeStartedTimestamp,
                        typeId = UNTRACKED_ITEM_ID,
                        tagIds = emptyList(),
                        data = Data.RecordData(it),
                    )
                }
            }
        } else {
            emptyList()
        }

        return dailyRecordFilterInteractor.filter(
            runningRecordsData = runningRecordsData,
            trackedRecordsData = trackedRecordsData,
            untrackedRecordsData = untrackedRecordsData,
            chartFilterType = filterType,
            filteredIds = filteredIds,
            lazyRecordTypeCategories = recordTypeCategories,
        )
    }

    private fun mapToCalendarPoint(
        holder: RecordHolder<Data>,
        calendar: Calendar,
        startOfDayShift: Long,
        rangeStart: Long,
        rangeEnd: Long,
        showSeconds: Boolean,
        multiSelectedIds: List<MultiSelectedRecordId>,
    ): RecordsCalendarViewData.Point {
        // Record data already clamped.
        val timeStartedTimestamp = when (holder.data) {
            is Data.RecordData ->
                holder.timeStartedTimestamp.let { if (showSeconds) it else it.dropSeconds() }
            is Data.RunningRecordData ->
                max(holder.timeStartedTimestamp, rangeStart)
        }
        val timeEndedTimestamp = when (val data = holder.data) {
            is Data.RecordData ->
                data.value.timeEndedTimestamp.let { if (showSeconds) it else it.dropSeconds() }
            is Data.RunningRecordData ->
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
            data = when (val data = holder.data) {
                is Data.RecordData -> {
                    RecordsCalendarViewData.Point.Data.RecordData(data.value)
                }
                is Data.RunningRecordData -> {
                    RecordsCalendarViewData.Point.Data.RunningRecordData(data.value)
                }
            },
        )
    }

    private fun mapMultiSelectedId(
        holder: RecordHolder<Data>,
    ): MultiSelectedRecordId {
        return when (val data = holder.data) {
            is Data.RecordData -> {
                when (val value = data.value) {
                    is RecordViewData.Tracked -> MultiSelectedRecordId.Tracked(value.id)
                    is RecordViewData.Untracked -> MultiSelectedRecordId.Untracked(
                        timeStartedTimestamp = value.timeStartedTimestamp,
                        timeEndedTimestamp = value.timeEndedTimestamp,
                    )
                }
            }
            is Data.RunningRecordData -> {
                val value = data.value
                MultiSelectedRecordId.Running(value.id)
            }
        }
    }

    private sealed interface Data {
        val value: ViewHolderType

        data class RecordData(
            override val value: RecordViewData,
        ) : Data

        data class RunningRecordData(
            override val value: RunningRecordViewData,
        ) : Data
    }

    private data class ViewDataIntermediate(
        val rangeStart: Long,
        val rangeEnd: Long,
        val isToday: Boolean,
        val records: List<RecordHolder<Data>>,
    )

    companion object {
        private val minuteInMillis = TimeUnit.MINUTES.toMillis(1)
    }
}