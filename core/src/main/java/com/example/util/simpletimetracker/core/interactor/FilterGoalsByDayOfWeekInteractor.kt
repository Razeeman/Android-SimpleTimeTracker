package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.filterDaysOfWeek
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import java.util.Calendar
import javax.inject.Inject

class FilterGoalsByDayOfWeekInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
    private val prefsInteractor: PrefsInteractor,
) {

    fun execute(
        goals: List<RecordTypeGoal>,
        range: Range,
        startOfDayShift: Long,
    ): List<RecordTypeGoal> {
        val dayOfWeek = timeMapper.getDayOfWeek(
            timestamp = range.timeStarted,
            calendar = Calendar.getInstance(),
            startOfDayShift = startOfDayShift,
        )
        return goals.filterDaysOfWeek(dayOfWeek)
    }

    suspend fun execute(
        goals: List<RecordTypeGoal>,
    ): List<RecordTypeGoal> {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        return execute(
            goals = goals,
            range = timeMapper.getRangeStartAndEnd(
                rangeLength = RangeLength.Day,
                shift = 0,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
            ),
            startOfDayShift = startOfDayShift,
        )
    }
}