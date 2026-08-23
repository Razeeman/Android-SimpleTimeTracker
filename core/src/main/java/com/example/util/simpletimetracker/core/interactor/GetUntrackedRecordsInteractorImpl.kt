package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.UntrackedRangeMapper
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.dropMillis
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.mapper.UntrackedRecordMapper
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import javax.inject.Inject

class GetUntrackedRecordsInteractorImpl @Inject constructor(
    private val untrackedRecordMapper: UntrackedRecordMapper,
    private val untrackedRangeMapper: UntrackedRangeMapper,
    private val recordInteractor: RecordInteractor,
    private val prefsInteractor: PrefsInteractor,
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
        val maxEnd = System.currentTimeMillis().dropMillis()

        // If range is all records - calculate from first records to current time.
        val actualRange = if (range.isUndefined) {
            Range(timeStarted = minStart, timeEnded = maxEnd)
        } else {
            range
        }
        val untrackedRanges = untrackedRecordMapper.calculateUntrackedRanges(
            records = records,
            range = actualRange,
            minStart = minStart,
            maxEnd = maxEnd,
            durationCutoff = durationCutoff,
        )
        val timeOfDay = if (prefsInteractor.getUntrackedRangeEnabled()) {
            Range(
                timeStarted = prefsInteractor.getUntrackedRangeStart(),
                timeEnded = prefsInteractor.getUntrackedRangeEnd(),
            )
        } else {
            null
        }

        return untrackedRangeMapper.processSettings(
            ranges = untrackedRanges,
            daysOfWeek = prefsInteractor.getUntrackedDaysOfWeek(),
            timeOfDay = timeOfDay,
        ).filter {
            // Reapply duration cutoff after time range and days of week filtering.
            untrackedRecordMapper.filter(it.duration, durationCutoff)
        }.map {
            Record(
                typeId = UNTRACKED_ITEM_ID,
                timeStarted = it.timeStarted,
                timeEnded = it.timeEnded,
                comment = "",
                tags = emptyList(),
            )
        }
    }
}