package com.example.util.simpletimetracker.feature_notification.scheduledReminder.controller

import com.example.util.simpletimetracker.domain.notifications.interactor.ScheduledReminderNotificationInteractor
import javax.inject.Inject

class ScheduledReminderController @Inject constructor(
    private val scheduledReminderInteractor: ScheduledReminderNotificationInteractor,
) {

    suspend fun onReminderFired(
        reminderId: Long,
        expectedOccurrenceTimestamp: Long,
    ) {
        if (reminderId == 0L) return
        scheduledReminderInteractor.onReminderFired(
            reminderId = reminderId,
            expectedOccurrenceTimestamp = expectedOccurrenceTimestamp,
        )
    }

    suspend fun onBootCompleted() {
        rescheduleAll()
    }

    suspend fun onExactAlarmPermissionStateChanged() {
        rescheduleAll()
    }

    suspend fun onPackageReplaced() {
        rescheduleAll()
    }

    suspend fun onDateTimeChanged() {
        rescheduleAll()
    }

    private suspend fun rescheduleAll() {
        scheduledReminderInteractor.rescheduleAll()
    }
}