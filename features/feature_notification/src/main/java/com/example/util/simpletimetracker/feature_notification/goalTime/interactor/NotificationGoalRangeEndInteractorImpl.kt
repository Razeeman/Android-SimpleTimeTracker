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

    override suspend fun checkAndRescheduleDaily() {
        val hasDailyGoals = recordTypeGoalInteractor.getAll()
            .any { it.range is RecordTypeGoal.Range.Daily }

        if (hasDailyGoals) {
            schedule(RecordTypeGoal.Range.Daily)
        } else {
            cancel(RecordTypeGoal.Range.Daily)
        }
    }

    override suspend fun schedule(range: RecordTypeGoal.Range) {
        cancel(range)

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

    override fun cancel(range: RecordTypeGoal.Range) {
        rangeEndScheduler.cancelSchedule(range)
    }
}