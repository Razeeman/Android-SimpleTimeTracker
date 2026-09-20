package com.example.util.simpletimetracker.feature_notification.goalTime.controller

import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import javax.inject.Inject

class NotificationGoalTimeBroadcastController @Inject constructor(
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
) {

    suspend fun onGoalTimeReminder(goalId: Long) {
        val goal = recordTypeGoalInteractor.get(goalId) ?: return
        if (goal.type !is RecordTypeGoal.Type.Duration) return
        notificationGoalTimeInteractor.show(goal)
        (goal.idData as? RecordTypeGoal.IdData.Type)?.let {
            externalViewsInteractor.onGoalTimeReached(it.value)
        }
    }

    suspend fun onRangeEndReminder() {
        reschedule()
        externalViewsInteractor.onGoalRangeEnd()
    }

    suspend fun onBootCompleted() {
        reschedule()
    }

    suspend fun onExactAlarmPermissionStateChanged() {
        reschedule()
    }

    suspend fun onPackageReplaced() {
        reschedule()
    }

    private suspend fun reschedule() {
        notificationGoalTimeInteractor.checkAndReschedule()
        notificationGoalTimeInteractor.checkAndRescheduleTags()
    }
}