package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
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
) {

    fun map(
        record: Record,
        recordType: RecordType,
        recordTags: List<RecordTag>,
        range: Range,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): RecordViewData.Tracked {
        val (timeStarted, timeEnded) = clampToRange(record, range)

        return recordViewDataMapper.map(
            record = record,
            recordType = recordType,
            recordTags = recordTags,
            timeStarted = timeStarted,
            timeEnded = timeEnded,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
    }

    fun mapToUntracked(
        record: Record,
        range: Range,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): RecordViewData.Untracked {
        val (timeStarted, timeEnded) = clampToRange(record, range)

        return recordViewDataMapper.mapToUntracked(
            timeStarted = timeStarted,
            timeEnded = timeEnded,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
    }

    fun mapToHint(): ViewHolderType {
        return HintViewData(
            text = R.string.records_hint.let(resourceRepo::getString),
        )
    }

    fun mapTitle(
        shift: Int,
        startOfDayShift: Long,
        isCalendarView: Boolean,
        calendarDayCount: Int,
    ): String {
        return if (isCalendarView && calendarDayCount > 1) {
            val start = shift * calendarDayCount - calendarDayCount + 1
            val end = shift * calendarDayCount

            timeMapper.toDayShortDateTitle(start, startOfDayShift) +
                " - " +
                timeMapper.toDayShortDateTitle(end, startOfDayShift)
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