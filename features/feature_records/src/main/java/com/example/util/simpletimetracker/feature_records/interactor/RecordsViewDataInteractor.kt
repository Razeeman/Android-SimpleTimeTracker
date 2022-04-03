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
import com.example.util.simpletimetracker.feature_records.mapper.RecordsViewDataMapper
import com.example.util.simpletimetracker.feature_records.model.RecordsState
import java.util.Calendar
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
        val recordTypes = recordTypeInteractor.getAll().map { it.id to it }.toMap()
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
                mapToCalendarRecord(record, calendar)
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

    private fun mapToCalendarRecord(
        record: RecordViewData,
        calendar: Calendar,
    ): RecordViewData {
        val start = calendar.apply {
            timeInMillis = record.timeStartedTimestamp
            setToStartOfDay()
        }.let {
            record.timeStartedTimestamp - it.timeInMillis
        }
        val end = start + (record.timeEndedTimestamp - record.timeStartedTimestamp)

        return when (record) {
            is RecordViewData.Tracked -> record.copy(
                timeStartedTimestamp = start,
                timeEndedTimestamp = end
            )
            is RecordViewData.Untracked -> record.copy(
                timeStartedTimestamp = start,
                timeEndedTimestamp = end
            )
        }
    }

    companion object {
        private const val UNTRACKED_RECORD_LENGTH_LIMIT: Long = 60 * 1000L // 1 min
    }
}