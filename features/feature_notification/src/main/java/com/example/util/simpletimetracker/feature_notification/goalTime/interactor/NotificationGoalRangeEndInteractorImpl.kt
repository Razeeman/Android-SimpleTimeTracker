package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.GetRangeInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalRangeEndInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_notification.goalTime.scheduler.NotificationRangeEndScheduler
import javax.inject.Inject

class NotificationGoalRangeEndInteractorImpl @Inject constructor(
    private val rangeEndScheduler: NotificationRangeEndScheduler,
    private val getRangeInteractor: GetRangeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
) : NotificationGoalRangeEndInteractor {

    override suspend fun checkAndReschedule() {
        cancel()

        val goals = recordTypeGoalInteractor.getAll()

        val hasDailyGoals = goals.any { it.range is RecordTypeGoal.Range.Daily }
        if (hasDailyGoals) {
            schedule(RecordTypeGoal.Range.Daily)
        }

        val hasWeeklyGoals = goals.any { it.range is RecordTypeGoal.Range.Weekly }
        if (hasWeeklyGoals) {
            schedule(RecordTypeGoal.Range.Weekly)
        }

        val hasMonthlyGoals = goals.any { it.range is RecordTypeGoal.Range.Monthly }
        if (hasMonthlyGoals) {
            schedule(RecordTypeGoal.Range.Monthly)
        }
    }

    override fun cancel() {
        listOf(
            RecordTypeGoal.Range.Daily,
            RecordTypeGoal.Range.Weekly,
            RecordTypeGoal.Range.Monthly,
        ).forEach {
            rangeEndScheduler.cancelSchedule(it)
        }
    }

    private suspend fun schedule(range: RecordTypeGoal.Range) {
        val forRange = when (range) {
            is RecordTypeGoal.Range.Session -> return
            is RecordTypeGoal.Range.Daily -> RangeLength.Day
            is RecordTypeGoal.Range.Weekly -> RangeLength.Week
            is RecordTypeGoal.Range.Monthly -> RangeLength.Month
        }.let { getRangeInteractor.getRange(it) }

        rangeEndScheduler.schedule(
            timestamp = forRange.timeEnded,
            goalRange = RecordTypeGoal.Range.Daily,
        )
    }
}