package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.domain.extension.getDailyCount
import com.example.util.simpletimetracker.domain.extension.getMonthlyCount
import com.example.util.simpletimetracker.domain.extension.getWeeklyCount
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalCountInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationGoalCountInteractorImpl @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val manager: NotificationGoalTimeManager,
    private val notificationGoalParamsInteractor: NotificationGoalParamsInteractor,
) : NotificationGoalCountInteractor {

    override suspend fun checkAndShow(typeId: Long) {
        val recordType = recordTypeInteractor.get(typeId)
        val runningRecord = runningRecordInteractor.get(typeId)
        val goals = recordTypeGoalInteractor.getByType(typeId)

        if (recordType == null || runningRecord == null) return

        cancel(typeId)

        // Daily
        val dailyGoalCount = goals.getDailyCount().value
        if (dailyGoalCount > 0) {
            val dailyCurrent = getCurrentRecordsDurationInteractor.getDailyCurrent(runningRecord)

            if (dailyGoalCount == dailyCurrent.count) {
                show(typeId, GoalTimeType.Day)
            }
        }

        // Weekly
        val weeklyGoalCount = goals.getWeeklyCount().value
        if (weeklyGoalCount > 0) {
            val weeklyCurrent = getCurrentRecordsDurationInteractor.getWeeklyCurrent(runningRecord)

            if (weeklyGoalCount == weeklyCurrent.count) {
                show(typeId, GoalTimeType.Week)
            }
        }

        // Monthly
        val monthlyGoalCount = goals.getMonthlyCount().value
        if (monthlyGoalCount > 0) {
            val monthlyCurrent = getCurrentRecordsDurationInteractor.getMonthlyCurrent(runningRecord)

            if (monthlyGoalCount == monthlyCurrent.count) {
                show(typeId, GoalTimeType.Month)
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
            manager.hide(typeId, it)
        }
    }

    private fun show(typeId: Long, goalTimeType: GoalTimeType) {
        GlobalScope.launch {
            notificationGoalParamsInteractor.execute(
                typeId = typeId,
                range = goalTimeType,
                type = NotificationGoalParamsInteractor.Type.Count,
            )?.let(manager::show)
        }
    }
}