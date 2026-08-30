package com.example.util.simpletimetracker.feature_notification.activity.controller

import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivityInteractor
import javax.inject.Inject

class NotificationActivityBroadcastController @Inject constructor(
    private val notificationActivityInteractor: NotificationActivityInteractor,
) {

    suspend fun onActivityReminder(
        activityId: Long,
        expectedTimerStart: Long,
        expectedTriggerTimestamp: Long,
    ) {
        notificationActivityInteractor.onReminderFired(
            activityId = activityId,
            expectedTimerStart = expectedTimerStart,
            expectedTriggerTimestamp = expectedTriggerTimestamp,
        )
    }

    suspend fun onBootCompleted() {
        notificationActivityInteractor.rescheduleAll()
    }

    suspend fun onPackageReplaced() {
        notificationActivityInteractor.rescheduleAll()
    }
}