package com.example.util.simpletimetracker.feature_records.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.model.count
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_records.customView.RecordsCalendarViewData
import com.example.util.simpletimetracker.feature_records.mapper.RecordsViewDataMapper
import com.example.util.simpletimetracker.feature_records.model.RecordsState
import java.lang.Long.min
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordsViewDataInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val getUntrackedRecordsInteractor: GetUntrackedRecordsInteractor,
    private val recordsViewDataMapper: RecordsViewDataMapper,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
) {

    suspend fun getViewData(shift: Int): RecordsState = withContext(Dispatchers.Default) {
        val calendar = Calendar.getInstance()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val showUntrackedInRecords = prefsInteractor.getShowUntrackedInRecords()
        val reverseOrder = prefsInteractor.getReverseOrderInCalendar()
        val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
        val recordTags = recordTagInteractor.getAll()
        val runningRecords = runningRecordInteractor.getAll()
        val isCalendarView = prefsInteractor.getShowRecordsCalendar()
        val calendarDayCount = prefsInteractor.getDaysInCalendar().count
        val daysCountInShift = if (isCalendarView) calendarDayCount else 1

        return@withContext (daysCountInShift - 1 downTo 0).map { dayInShift ->
            val actualShift = shift * daysCountInShift - dayInShift

            val (rangeStart, rangeEnd) = timeMapper.getRangeStartAndEnd(
                rangeLength = RangeLength.Day,
                shift = actualShift,
                firstDayOfWeek = DayOfWeek.MONDAY, // Doesn't matter for days.
                startOfDayShift = startOfDayShift,
            )
            val records = recordInteractor.getFromRange(Range(rangeStart, rangeEnd))

            val data = getRecordsViewData(
                records = records,
                runningRecords = runningRecords,
                recordTypes = recordTypes,
                recordTags = recordTags,
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                useProportionalMinutes = useProportionalMinutes,
                showUntrackedInRecords = showUntrackedInRecords,
                showSeconds = showSeconds,
            )

            ViewDataIntermediate(
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
                records = data,
            )
        }.let { data ->
            if (isCalendarView) {
                mapCalendarData(
                    data = data,
                    calendar = calendar,
                    startOfDayShift = startOfDayShift,
                    shift = shift,
                    reverseOrder = reverseOrder
                )
            } else {
                mapRecordsData(data)
            }
        }
    }

    private fun mapCalendarData(
        data: List<ViewDataIntermediate>,
        calendar: Calendar,
        startOfDayShift: Long,
        shift: Int,
        reverseOrder: Boolean
    ): RecordsState.CalendarData {
        val currentTime = if (shift == 0) {
            mapFromStartOfDay(
                timeStamp = System.currentTimeMillis(),
                calendar = calendar
            ) - startOfDayShift
        } else {
            null
        }

        return data
            .map {
                it.records.map { record ->
                    mapToCalendarPoint(
                        holder = record,
                        calendar = calendar,
                        startOfDayShift = startOfDayShift,
                        rangeStart = it.rangeStart,
                        rangeEnd = it.rangeEnd,
                    )
                }
            }
            .let {
                RecordsCalendarViewData(
                    currentTime = currentTime,
                    startOfDayShift = startOfDayShift,
                    points = it,
                    reverseOrder = reverseOrder,
                )
            }
            .let(RecordsState::CalendarData)
    }

    private fun mapRecordsData(
        data: List<ViewDataIntermediate>,
    ): RecordsState.RecordsData {
        return data
            .firstOrNull()
            ?.records
            .orEmpty()
            .sortedByDescending { it.timeStartedTimestamp }
            .map { it.data.value }
            .let {
                if (it.isEmpty()) {
                    listOf(recordsViewDataMapper.mapToEmpty())
                } else {
                    it + recordsViewDataMapper.mapToHint()
                }
            }
            .let(RecordsState::RecordsData)
    }

    private suspend fun getRecordsViewData(
        records: List<Record>,
        runningRecords: List<RunningRecord>,
        recordTypes: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
        rangeStart: Long,
        rangeEnd: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showUntrackedInRecords: Boolean,
        showSeconds: Boolean,
    ): List<RecordHolder> {
        val trackedRecordsData = records
            .mapNotNull { record ->
                recordsViewDataMapper.map(
                    record = record,
                    recordType = recordTypes[record.typeId] ?: return@mapNotNull null,
                    recordTags = recordTags.filter { it.id in record.tagIds },
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                ).let {
                    RecordHolder(
                        timeStartedTimestamp = it.timeStartedTimestamp,
                        data = RecordHolder.Data.RecordData(it),
                    )
                }
            }

        val runningRecordsData = runningRecords
            .let {
                rangeMapper.getRunningRecordsFromRange(
                    records = it,
                    range = Range(rangeStart, rangeEnd),
                )
            }
            .mapNotNull { runningRecord ->
                getRunningRecordViewDataMediator.execute(
                    type = recordTypes[runningRecord.id] ?: return@mapNotNull null,
                    tags = recordTags.filter { it.id in runningRecord.tagIds },
                    record = runningRecord,
                    nowIconVisible = true,
                    goalsVisible = false,
                    totalDurationVisible = false,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                ).let {
                    RecordHolder(
                        timeStartedTimestamp = runningRecord.timeStarted,
                        data = RecordHolder.Data.RunningRecordData(it),
                    )
                }
            }

        val untrackedRecordsData = if (showUntrackedInRecords) {
            val recordRanges = records.map {
                Range(
                    timeStarted = it.timeStarted,
                    timeEnded = it.timeEnded
                )
            }
            val runningRecordRanges = runningRecords.map {
                Range(
                    timeStarted = it.timeStarted,
                    timeEnded = System.currentTimeMillis(),
                )
            }
            getUntrackedRecordsInteractor.get(
                range = Range(rangeStart, rangeEnd),
                records = recordRanges + runningRecordRanges,
            ).map { untrackedRecord ->
                recordsViewDataMapper.mapToUntracked(
                    record = untrackedRecord,
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                ).let {
                    RecordHolder(
                        timeStartedTimestamp = it.timeStartedTimestamp,
                        data = RecordHolder.Data.RecordData(it),
                    )
                }
            }
        } else {
            emptyList()
        }

        return trackedRecordsData + runningRecordsData + untrackedRecordsData
    }

    private fun mapToCalendarPoint(
        holder: RecordHolder,
        calendar: Calendar,
        startOfDayShift: Long,
        rangeStart: Long,
        rangeEnd: Long,
    ): RecordsCalendarViewData.Point {
        // Record data already clamped.
        val timeStartedTimestamp = when (holder.data) {
            is RecordHolder.Data.RecordData -> holder.timeStartedTimestamp
            is RecordHolder.Data.RunningRecordData -> max(holder.timeStartedTimestamp, rangeStart)
        }
        val timeEndedTimestamp = when (holder.data) {
            is RecordHolder.Data.RecordData -> holder.data.value.timeEndedTimestamp
            is RecordHolder.Data.RunningRecordData -> min(System.currentTimeMillis(), rangeEnd)
        }

        val start = mapFromStartOfDay(
            // Normalize to set start of day correctly.
            timeStamp = timeStartedTimestamp - startOfDayShift,
            calendar = calendar,
        ) + startOfDayShift

        val duration = (timeEndedTimestamp - timeStartedTimestamp)
            // Otherwise would be invisible.
            .takeUnless { it == 0L } ?: minuteInMillis

        val end = start + duration

        return RecordsCalendarViewData.Point(
            start = start - startOfDayShift,
            end = end - startOfDayShift,
            data = when (holder.data) {
                is RecordHolder.Data.RecordData -> {
                    RecordsCalendarViewData.Point.Data.RecordData(holder.data.value)
                }
                is RecordHolder.Data.RunningRecordData -> {
                    RecordsCalendarViewData.Point.Data.RunningRecordData(holder.data.value)
                }
            }
        )
    }

    private fun mapFromStartOfDay(
        timeStamp: Long,
        calendar: Calendar,
    ): Long {
        return calendar.apply {
            timeInMillis = timeStamp
            setToStartOfDay()
        }.let {
            timeStamp - it.timeInMillis
        }
    }

    private data class RecordHolder(
        val timeStartedTimestamp: Long,
        val data: Data,
    ) {
        sealed interface Data {
            val value: ViewHolderType

            data class RecordData(override val value: RecordViewData) : Data
            data class RunningRecordData(override val value: RunningRecordViewData) : Data
        }
    }

    private data class ViewDataIntermediate(
        val rangeStart: Long,
        val rangeEnd: Long,
        val records: List<RecordHolder>,
    )

    companion object {
        private val minuteInMillis = TimeUnit.MINUTES.toMillis(1)
    }
}