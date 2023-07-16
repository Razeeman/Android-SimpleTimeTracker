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
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeManager
import com.example.util.simpletimetracker.feature_notification.goalTime.scheduler.NotificationGoalTimeScheduler
import com.example.util.simpletimetracker.feature_notification.goalTime.scheduler.NotificationRangeEndScheduler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
            GoalTimeType.Day,
            GoalTimeType.Week,
            GoalTimeType.Month,
        ).forEach(rangeEndScheduler::cancelSchedule)

        // Session
        val sessionCurrent = System.currentTimeMillis() - runningRecord.timeStarted
        val sessionGoalTime = goals.getSessionDuration().value * 1000
        if (sessionGoalTime > 0L && sessionGoalTime > sessionCurrent) {
            scheduler.schedule(sessionGoalTime - sessionCurrent, typeId, GoalTimeType.Session)
        }

        // Daily
        val dailyGoalTime = goals.getDailyDuration().value * 1000
        if (dailyGoalTime > 0) {
            val dailyCurrent = getCurrentRecordsDurationInteractor.getDailyCurrent(runningRecord)

            if (dailyGoalTime > dailyCurrent.duration) {
                scheduler.schedule(
                    durationMillisFromNow = dailyGoalTime - dailyCurrent.duration,
                    typeId = typeId,
                    goalTimeType = GoalTimeType.Day,
                )
                rangeEndScheduler.schedule(dailyCurrent.range.timeEnded, GoalTimeType.Day)
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
                    goalTimeType = GoalTimeType.Week,
                )
                rangeEndScheduler.schedule(weeklyCurrent.range.timeEnded, GoalTimeType.Week)
            }
        }

        // Monthly
        val monthlyGoalTime = goals.getMonthlyDuration().value * 1000
        if (monthlyGoalTime > 0) {
            val monthlyCurrent = getCurrentRecordsDurationInteractor.getMonthlyCurrent(runningRecord)
            if (monthlyGoalTime > monthlyCurrent.duration) {
                scheduler.schedule(
                    durationMillisFromNow = monthlyGoalTime - monthlyCurrent.duration,
                    typeId = typeId,
                    goalTimeType = GoalTimeType.Month,
                )
                rangeEndScheduler.schedule(monthlyCurrent.range.timeEnded, GoalTimeType.Month)
            }
        }
    }

    override fun cancel(typeId: Long) {
        listOf(
            GoalTimeType.Session,
            GoalTimeType.Day,
            GoalTimeType.Week,
            GoalTimeType.Month,
        ).forEach {
            scheduler.cancelSchedule(typeId, it)
            manager.hide(typeId, it)
        }
    }

    override fun show(typeId: Long, goalTimeType: GoalTimeType) {
        GlobalScope.launch {
            notificationGoalParamsInteractor.execute(
                typeId = typeId,
                range = goalTimeType,
                type = NotificationGoalParamsInteractor.Type.Duration,
            )?.let(manager::show)
        }
    }
}