package com.example.util.simpletimetracker.feature_notification.scheduledReminder.controller

import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.domain.notifications.interactor.ScheduledReminderNotificationInteractor
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ScheduledReminderController @Inject constructor(
    private val scheduledReminderInteractor: ScheduledReminderNotificationInteractor,
) {

    fun onReminderFired(
        reminderId: Long,
        expectedOccurrenceTimestamp: Long,
    ) = allowDiskRead { MainScope() }.launch {
        if (reminderId == 0L) return@launch
        scheduledReminderInteractor.onReminderFired(
            reminderId = reminderId,
            expectedOccurrenceTimestamp = expectedOccurrenceTimestamp,
        )
    }

    fun rescheduleAll() = allowDiskRead { MainScope() }.launch {
        scheduledReminderInteractor.rescheduleAll()
    }
}