package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.daysOfWeek.mapper.DaysInCalendarMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_records.R
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class RecordsViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val timeMapper: TimeMapper,
    private val calendarToListShiftMapper: CalendarToListShiftMapper,
    private val daysInCalendarMapper: DaysInCalendarMapper,
) {

    fun map(
        record: Record,
        recordType: RecordType?,
        recordTags: List<RecordTag>,
        range: Range,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): RecordViewData.Tracked {
        val (timeStarted, timeEnded) = clampToRange(record, range)

        return recordViewDataMapper.map(
            record = record.copy(
                timeStarted = timeStarted,
                timeEnded = timeEnded,
            ),
            recordType = recordType,
            recordTags = recordTags,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
        )
    }

    fun mapToUntracked(
        record: Record,
        range: Range,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): RecordViewData.Untracked {
        val (timeStarted, timeEnded) = clampToRange(record, range)

        return recordViewDataMapper.mapToUntracked(
            timeStarted = timeStarted,
            timeEnded = timeEnded,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
        )
    }

    fun mapToHint(): ViewHolderType {
        return HintViewData(
            text = R.string.records_hint.let(resourceRepo::getString),
        )
    }

    fun mapToShareCalendarTitle(
        shift: Int,
        startOfDayShift: Long,
        isCalendarView: Boolean,
        daysInCalendar: DaysInCalendar,
        firstDayOfWeek: DayOfWeek,
    ): String {
        val calendarDayCount = daysInCalendarMapper.mapDaysCount(daysInCalendar)
        return if (isCalendarView && calendarDayCount > 1) {
            val calendarRange = calendarToListShiftMapper.mapCalendarToListShift(
                calendarShift = shift,
                daysInCalendar = daysInCalendar,
                startOfDayShift = startOfDayShift,
                firstDayOfWeek = firstDayOfWeek,
            )

            timeMapper.toDayShortDateTitle(calendarRange.start, startOfDayShift) +
                " - " +
                timeMapper.toDayShortDateTitle(calendarRange.end, startOfDayShift)
        } else {
            timeMapper.toDayTitle(shift, startOfDayShift)
        }
    }

    private fun clampToRange(
        record: Record,
        range: Range,
    ): Range {
        val timeStarted = if (range.timeStarted != 0L) {
            max(record.timeStarted, range.timeStarted)
        } else {
            record.timeStarted
        }
        val timeEnded = if (range.timeEnded != 0L) {
            min(record.timeEnded, range.timeEnded)
        } else {
            record.timeEnded
        }

        return Range(timeStarted, timeEnded)
    }
}