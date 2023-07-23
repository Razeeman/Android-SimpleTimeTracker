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
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
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
        if (dailyGoalCount > 1) {
            val dailyCurrent = getCurrentRecordsDurationInteractor.getDailyCurrent(runningRecord)

            if (dailyGoalCount == dailyCurrent.count) {
                show(typeId, RecordTypeGoal.Range.Daily)
            }
        }

        // Weekly
        val weeklyGoalCount = goals.getWeeklyCount().value
        if (weeklyGoalCount > 1) {
            val weeklyCurrent = getCurrentRecordsDurationInteractor.getWeeklyCurrent(runningRecord)

            if (weeklyGoalCount == weeklyCurrent.count) {
                show(typeId, RecordTypeGoal.Range.Weekly)
            }
        }

        // Monthly
        val monthlyGoalCount = goals.getMonthlyCount().value
        if (monthlyGoalCount > 1) {
            val monthlyCurrent = getCurrentRecordsDurationInteractor.getMonthlyCurrent(runningRecord)

            if (monthlyGoalCount == monthlyCurrent.count) {
                show(typeId, RecordTypeGoal.Range.Monthly)
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
            manager.hide(typeId, it)
        }
    }

    private fun show(typeId: Long, goalRange: RecordTypeGoal.Range) {
        GlobalScope.launch {
            notificationGoalParamsInteractor.execute(
                typeId = typeId,
                range = goalRange,
                type = NotificationGoalParamsInteractor.Type.Count,
            )?.let(manager::show)
        }
    }
}