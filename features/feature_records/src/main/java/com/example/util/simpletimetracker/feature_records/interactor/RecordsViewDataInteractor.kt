package com.example.util.simpletimetracker.feature_records.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_records.customView.RecordsCalendarViewData
import com.example.util.simpletimetracker.feature_records.mapper.RecordsViewDataMapper
import com.example.util.simpletimetracker.feature_records.model.RecordsState
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecordsViewDataInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordsViewDataMapper: RecordsViewDataMapper,
    private val timeMapper: TimeMapper,
) {

    suspend fun getViewData(shift: Int): RecordsState {
        val calendar = Calendar.getInstance()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val isCalendarView = prefsInteractor.getShowRecordsCalendar()
        val showUntrackedInRecords = prefsInteractor.getShowUntrackedInRecords()
        val reverseOrder = prefsInteractor.getReverseOrderInCalendar()
        val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
        val recordTags = recordTagInteractor.getAll()
        val (rangeStart, rangeEnd) = timeMapper.getRangeStartAndEnd(
            rangeLength = RangeLength.Day,
            shift = shift,
            firstDayOfWeek = DayOfWeek.MONDAY, // Doesn't matter for days.
            startOfDayShift = startOfDayShift,
        )
        val records = if (rangeStart != 0L && rangeEnd != 0L) {
            recordInteractor.getFromRange(rangeStart, rangeEnd)
        } else {
            recordInteractor.getAll()
        }

        val recordsViewData = getRecordsViewData(
            records = records,
            recordTypes = recordTypes,
            recordTags = recordTags,
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            useProportionalMinutes = useProportionalMinutes,
            showUntrackedInRecords = showUntrackedInRecords,
        )

        if (isCalendarView) return recordsViewData
            .map { record ->
                mapToCalendarPoint(record, calendar, startOfDayShift)
            }
            .let {
                val currentTime = if (shift == 0) {
                    mapFromStartOfDay(
                        timeStamp = System.currentTimeMillis(),
                        calendar = calendar
                    ) - startOfDayShift
                } else {
                    null
                }
                RecordsCalendarViewData(
                    currentTime = currentTime,
                    startOfDayShift = startOfDayShift,
                    points = it,
                    reverseOrder = reverseOrder,
                )
            }
            .let(RecordsState::CalendarData)

        return recordsViewData
            .sortedByDescending { it.timeStartedTimestamp }
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
        recordTypes: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
        rangeStart: Long,
        rangeEnd: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showUntrackedInRecords: Boolean,
    ): List<RecordViewData> {
        return records
            .mapNotNull { record ->
                recordsViewDataMapper.map(
                    record = record,
                    recordType = recordTypes[record.typeId] ?: return@mapNotNull null,
                    recordTags = recordTags.filter { it.id in record.tagIds },
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes
                )
            }
            .let { trackedRecords ->
                if (!showUntrackedInRecords) return@let trackedRecords

                recordInteractor.getUntrackedFromRange(rangeStart, rangeEnd)
                    .filter {
                        // Filter only untracked records that are longer than a minute
                        (it.timeEnded - it.timeStarted) >= UNTRACKED_RECORD_LENGTH_LIMIT
                    }
                    .map { untrackedRecord ->
                        recordsViewDataMapper.mapToUntracked(
                            record = untrackedRecord,
                            rangeStart = rangeStart,
                            rangeEnd = rangeEnd,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            useProportionalMinutes = useProportionalMinutes
                        )
                    }
                    .let { untrackedRecords -> trackedRecords + untrackedRecords }
            }
    }

    private fun mapToCalendarPoint(
        record: RecordViewData,
        calendar: Calendar,
        startOfDayShift: Long,
    ): RecordsCalendarViewData.Point {
        val start = mapFromStartOfDay(
            // Normalize to set start of day correctly.
            timeStamp = record.timeStartedTimestamp - startOfDayShift,
            calendar = calendar,
        ) + startOfDayShift
        val duration = (record.timeEndedTimestamp - record.timeStartedTimestamp)
            // Otherwise would be invisible.
            .takeUnless { it == 0L } ?: minuteInMillis
        val end = start + duration

        return RecordsCalendarViewData.Point(
            start = start - startOfDayShift,
            end = end - startOfDayShift,
            data = record
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

    companion object {
        private const val UNTRACKED_RECORD_LENGTH_LIMIT: Long = 60 * 1000L // 1 min
        private val minuteInMillis = TimeUnit.MINUTES.toMillis(1)
    }
}