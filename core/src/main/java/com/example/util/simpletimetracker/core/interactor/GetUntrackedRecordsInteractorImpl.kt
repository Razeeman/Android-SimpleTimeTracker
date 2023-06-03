package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.mapper.UntrackedRecordMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class GetUntrackedRecordsInteractorImpl @Inject constructor(
    private val untrackedRecordMapper: UntrackedRecordMapper,
    private val recordInteractor: RecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
) : GetUntrackedRecordsInteractor {

    override suspend fun get(
        range: Range,
        records: List<Range>,
    ): List<Record> {
        val durationCutoff = prefsInteractor.getIgnoreShortUntrackedDuration()
        // Calculate from first record. No records - don't calculate.
        val minStart = recordInteractor.getNext(0)?.timeStarted ?: return emptyList()
        // Bound end range of calculation to current time,
        // to not show untracked time in the future
        val maxEnd = System.currentTimeMillis()

        // If range is all records - calculate from first records to current time.
        val actualRange = if (range.timeStarted == 0L && range.timeEnded == 0L) {
            Range(timeStarted = minStart, timeEnded = maxEnd)
        } else {
            range
        }
        return untrackedRecordMapper.calculateUntrackedRanges(
            records = records,
            range = actualRange,
            minStart = minStart,
            maxEnd = maxEnd,
            durationCutoff = durationCutoff,
        ).map {
            Record(
                typeId = UNTRACKED_ITEM_ID,
                timeStarted = it.first,
                timeEnded = it.second,
                comment = ""
            )
        }.let {
            processTimeOfDayRange(it)
        }.filter {
            untrackedRecordMapper.filter(it.duration, durationCutoff)
        }
    }

    private suspend fun processTimeOfDayRange(
        records: List<Record>,
    ): List<Record> {
        val enabled = prefsInteractor.getUntrackedRangeEnabled()
        if (!enabled) return records
        val timeOfDay = Range(
            timeStarted = prefsInteractor.getUntrackedRangeStart(),
            timeEnded = prefsInteractor.getUntrackedRangeEnd(),
        )
        if (timeOfDay.duration == 0L) return records
        val calendar = Calendar.getInstance()

        return records.map {
            processRecord(
                record = it,
                timeOfDay = timeOfDay,
                calendar = calendar
            )
        }.flatten()
    }

    private fun processRecord(
        record: Record,
        timeOfDay: Range,
        calendar: Calendar,
    ): List<Record> {
        val result = mutableListOf<Record>()

        var check = true
        val recordStartOfDay = timeMapper.getStartOfDayTimeStamp(record.timeStarted, calendar)
        var currentTimeOfDayRangeStart = if (timeOfDay.timeStarted < timeOfDay.timeEnded) {
            recordStartOfDay + timeOfDay.timeStarted
        } else {
            val prevStartOfDay = calendar.apply {
                timeInMillis = recordStartOfDay
                add(Calendar.DATE, -1)
            }.timeInMillis
            prevStartOfDay + timeOfDay.timeStarted
        }
        var currentTimeOfDayRangeEnd = recordStartOfDay + timeOfDay.timeEnded

        while (check) {
            cutRangeFromRecord(
                record, Range(currentTimeOfDayRangeStart, currentTimeOfDayRangeEnd)
            )?.let(result::add)

            if (currentTimeOfDayRangeEnd >= record.timeEnded) {
                check = false
            } else {
                currentTimeOfDayRangeStart = calendar.apply {
                    timeInMillis = currentTimeOfDayRangeStart
                    add(Calendar.DATE, 1)
                }.timeInMillis
                currentTimeOfDayRangeEnd = calendar.apply {
                    timeInMillis = currentTimeOfDayRangeEnd
                    add(Calendar.DATE, 1)
                }.timeInMillis
            }
        }

        return result
    }

    private fun cutRangeFromRecord(
        record: Record,
        range: Range,
    ): Record? {
        return if (record.timeStarted < range.timeEnded && record.timeEnded > range.timeStarted) {
            record.copy(
                timeStarted = max(record.timeStarted, range.timeStarted),
                timeEnded = min(record.timeEnded, range.timeEnded)
            )
        } else {
            null
        }
    }
}