package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.domain.extension.getDailyDuration
import com.example.util.simpletimetracker.domain.extension.getMonthlyDuration
import com.example.util.simpletimetracker.domain.extension.getSessionDuration
import com.example.util.simpletimetracker.domain.extension.getWeeklyDuration
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeManager
import com.example.util.simpletimetracker.feature_notification.goalTime.scheduler.NotificationGoalTimeScheduler
import com.example.util.simpletimetracker.feature_notification.goalTime.scheduler.NotificationRangeEndScheduler
import javax.inject.Inject

class NotificationGoalTimeInteractorImpl @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val manager: NotificationGoalTimeManager,
    private val scheduler: NotificationGoalTimeScheduler,
    private val rangeEndScheduler: NotificationRangeEndScheduler,
    private val notificationGoalParamsInteractor: NotificationGoalParamsInteractor,
) : NotificationGoalTimeInteractor {

    override suspend fun checkAndReschedule() {
        runningRecordInteractor.getAll().forEach {
            checkAndReschedule(it.id)
        }
    }

    override suspend fun checkAndReschedule(typeId: Long) {
        val recordType = recordTypeInteractor.get(typeId)
        val runningRecord = runningRecordInteractor.get(typeId)
        val goals = recordTypeGoalInteractor.getByType(typeId)

        if (recordType == null || runningRecord == null) return

        cancel(typeId)

        listOf(
            RecordTypeGoal.Range.Daily,
            RecordTypeGoal.Range.Weekly,
            RecordTypeGoal.Range.Monthly,
        ).forEach { rangeEndScheduler.cancelSchedule(it) }

        // Session
        val sessionCurrent = System.currentTimeMillis() - runningRecord.timeStarted
        val sessionGoalTime = goals.getSessionDuration().value * 1000
        if (sessionGoalTime > 0L && sessionGoalTime > sessionCurrent) {
            scheduler.schedule(
                durationMillisFromNow = sessionGoalTime - sessionCurrent,
                typeId = typeId,
                goalRange = RecordTypeGoal.Range.Session,
            )
        }

        // Daily
        val dailyGoalTime = goals.getDailyDuration().value * 1000
        if (dailyGoalTime > 0) {
            val dailyCurrent = getCurrentRecordsDurationInteractor.getDailyCurrent(runningRecord)

            if (dailyGoalTime > dailyCurrent.duration) {
                scheduler.schedule(
                    durationMillisFromNow = dailyGoalTime - dailyCurrent.duration,
                    typeId = typeId,
                    goalRange = RecordTypeGoal.Range.Daily,
                )
                rangeEndScheduler.schedule(
                    timestamp = dailyCurrent.range.timeEnded,
                    goalRange = RecordTypeGoal.Range.Daily,
                )
            }
        }

        // Weekly
        val weeklyGoalTime = goals.getWeeklyDuration().value * 1000
        if (weeklyGoalTime > 0) {
            val weeklyCurrent = getCurrentRecordsDurationInteractor.getWeeklyCurrent(runningRecord)
            if (weeklyGoalTime > weeklyCurrent.duration) {
                scheduler.schedule(
                    durationMillisFromNow = weeklyGoalTime - weeklyCurrent.duration,
                    typeId = typeId,
                    goalRange = RecordTypeGoal.Range.Weekly,
                )
                rangeEndScheduler.schedule(
                    timestamp = weeklyCurrent.range.timeEnded,
                    goalRange = RecordTypeGoal.Range.Weekly,
                )
            }
        }

        // Monthly
        val monthlyGoalTime = goals.getMonthlyDuration().value * 1000
        if (monthlyGoalTime > 0) {
            val monthlyCurrent =
                getCurrentRecordsDurationInteractor.getMonthlyCurrent(runningRecord)
            if (monthlyGoalTime > monthlyCurrent.duration) {
                scheduler.schedule(
                    durationMillisFromNow = monthlyGoalTime - monthlyCurrent.duration,
                    typeId = typeId,
                    goalRange = RecordTypeGoal.Range.Monthly,
                )
                rangeEndScheduler.schedule(
                    monthlyCurrent.range.timeEnded,
                    RecordTypeGoal.Range.Monthly,
                )
            }
        }
    }

    override fun cancel(typeId: Long) {
        listOf(
            RecordTypeGoal.Range.Session,
            RecordTypeGoal.Range.Daily,
            RecordTypeGoal.Range.Weekly,
            RecordTypeGoal.Range.Monthly,
        ).forEach {
            scheduler.cancelSchedule(typeId, it)
            manager.hide(typeId, it)
        }
    }

    override suspend fun show(typeId: Long, goalRange: RecordTypeGoal.Range) {
        notificationGoalParamsInteractor.execute(
            typeId = typeId,
            range = goalRange,
            type = NotificationGoalParamsInteractor.Type.Duration,
        )?.let(manager::show)
    }
}