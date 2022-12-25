package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RunningRecord
import java.lang.Long.max
import javax.inject.Inject

class GetCurrentRecordsDurationInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val rangeMapper: RangeMapper,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
) {

    suspend fun getDailyCurrent(runningRecord: RunningRecord): Long {
        return getRangeCurrent(runningRecord, getRange(RangeLength.Day))
    }

    suspend fun getWeeklyCurrent(runningRecord: RunningRecord): Long {
        return getRangeCurrent(runningRecord, getRange(RangeLength.Week))
    }

    suspend fun getRangeCurrent(
        runningRecord: RunningRecord,
        range: Range
    ): Long {
        val (rangeStart, rangeEnd) = range
        // Clamp current running record
        val currentRunning = System.currentTimeMillis() - max(runningRecord.timeStarted, range.timeStarted)

        return recordInteractor.getFromRange(rangeStart, rangeEnd)
            .filter { it.typeId == runningRecord.id }
            .map { rangeMapper.clampToRange(it, rangeStart, rangeEnd) }
            .let(rangeMapper::mapToDuration) + currentRunning
    }

    suspend fun getRange(rangeLength: RangeLength): Range {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        return timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = 0,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        ).let { (start, end) ->
            Range(
                timeStarted = start,
                timeEnded = end
            )
        }
    }
}