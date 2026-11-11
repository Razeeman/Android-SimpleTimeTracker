package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.recordType.extension.adjustedValue
import com.example.util.simpletimetracker.domain.recordType.extension.isReached
import com.example.util.simpletimetracker.domain.recordType.extension.toRangeLength
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.Statistics
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import javax.inject.Inject

class GetGoalCompletionEstimateInteractor @Inject constructor(
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    fun get(
        goals: List<RecordTypeGoal>,
        statistics: List<Statistics>,
        filterType: ChartFilterType,
        rangeLength: RangeLength,
        range: Range,
    ): Map<Long, Estimate> {
        val now = currentTimestampProvider.get()
        if (!range.contains(now)) return emptyMap()

        val supported = goals.filter {
            it.type is RecordTypeGoal.Type.Duration &&
                it.type.value > 0 &&
                it.range.toRangeLength() == rangeLength
        }
        if (supported.isEmpty()) return emptyMap()

        val currents = statistics.associate { statistic ->
            val idData = when (filterType) {
                ChartFilterType.ACTIVITY -> RecordTypeGoal.IdData.Type(statistic.id)
                ChartFilterType.CATEGORY -> RecordTypeGoal.IdData.Category(statistic.id)
                ChartFilterType.RECORD_TAG -> RecordTypeGoal.IdData.Tag(statistic.id)
            }
            idData to statistic.data
        }

        return supported.mapNotNull { goal ->
            val current = currents[goal.idData] ?: return@mapNotNull null
            val estimate = estimate(
                goal = goal,
                current = current,
                range = range,
                now = now,
            ) ?: return@mapNotNull null
            goal.id to estimate
        }.toMap()
    }

    private fun estimate(
        goal: RecordTypeGoal,
        current: Statistics.Data,
        range: Range,
        now: Long,
    ): Estimate? {
        if (current.runningCount == 0L) return null

        val target = goal.adjustedValue
        if (goal.subtype.isReached(current.duration, target)) return null

        val remaining = target - current.duration

        // Each running timer adds 1 ms of progress per elapsed millisecond.
        // Round up for goals:
        // with 1,000 ms remaining and 3 timers, 333 ms adds only 999 ms
        // of progress, so reaching the target takes 334 ms.
        // Limits must strictly exceed the target, so add 1 ms even when the division is exact.
        // Although the estimate text omits seconds, this precision matters when
        // checking the period end below: rounding down could incorrectly place
        // completion before a reset when the target is only reached at the reset.
        val delay = when (goal.subtype) {
            RecordTypeGoal.Subtype.Goal -> {
                remaining / current.runningCount + if (remaining % current.runningCount == 0L) 0 else 1
            }
            RecordTypeGoal.Subtype.Limit -> {
                remaining / current.runningCount + 1
            }
        }

        val completion = now + delay

        return if (!range.isUndefined && completion >= range.timeEnded) {
            Estimate.NotReached
        } else {
            Estimate.Completion(timestamp = completion, currentTimestamp = now)
        }
    }

    sealed interface Estimate {
        data class Completion(val timestamp: Long, val currentTimestamp: Long) : Estimate
        data object NotReached : Estimate
    }
}
