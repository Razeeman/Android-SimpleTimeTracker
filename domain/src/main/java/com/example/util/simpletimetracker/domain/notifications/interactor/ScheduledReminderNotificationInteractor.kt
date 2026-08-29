package com.example.util.simpletimetracker.domain.notifications.interactor

interface ScheduledReminderNotificationInteractor {

    suspend fun schedule(reminderId: Long)

    fun cancel(reminderId: Long)

    suspend fun rescheduleAll()

    suspend fun onReminderFired(
        reminderId: Long,
        expectedOccurrenceTimestamp: Long,
    )
}
