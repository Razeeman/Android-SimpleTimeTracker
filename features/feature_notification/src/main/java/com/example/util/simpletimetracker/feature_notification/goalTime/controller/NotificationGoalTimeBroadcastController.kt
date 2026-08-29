package com.example.util.simpletimetracker.feature_notification.goalTime.controller

import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import javax.inject.Inject

class NotificationGoalTimeBroadcastController @Inject constructor(
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun onGoalTimeReminder(
        idData: RecordTypeGoal.IdData,
        goalRange: RecordTypeGoal.Range,
    ) {
        notificationGoalTimeInteractor.show(idData, goalRange)
        if (idData is RecordTypeGoal.IdData.Type) {
            externalViewsInteractor.onGoalTimeReached(idData.value)
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

    private suspend fun reschedule() {
        notificationGoalTimeInteractor.checkAndReschedule()
    }
}