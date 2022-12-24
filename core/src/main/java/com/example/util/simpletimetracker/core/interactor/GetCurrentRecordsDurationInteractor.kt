package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import javax.inject.Inject

class GetCurrentRecordsDurationInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val rangeMapper: RangeMapper,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
) {

    suspend fun getDailyCurrent(typeId: Long): Long {
        val (todayRangeStart, todayRangeEnd) = getRange(RangeLength.Day)

        return recordInteractor.getFromRange(todayRangeStart, todayRangeEnd)
            .filter { it.typeId == typeId }
            .map { rangeMapper.clampToRange(it, todayRangeStart, todayRangeEnd) }
            .let(rangeMapper::mapToDuration)
    }

    suspend fun getWeeklyCurrent(typeId: Long): Long {
        val (weekRangeStart, weekRangeEnd) = getRange(RangeLength.Week)

        return recordInteractor.getFromRange(weekRangeStart, weekRangeEnd)
            .filter { it.typeId == typeId }
            .map { rangeMapper.clampToRange(it, weekRangeStart, weekRangeEnd) }
            .let(rangeMapper::mapToDuration)
    }

    private suspend fun getRange(rangeLength: RangeLength): Pair<Long, Long> {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        return timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = 0,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
    }
}