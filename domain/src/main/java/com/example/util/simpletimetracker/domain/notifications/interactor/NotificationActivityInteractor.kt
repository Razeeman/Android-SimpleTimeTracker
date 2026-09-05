package com.example.util.simpletimetracker.domain.notifications.interactor

interface NotificationActivityInteractor {

    suspend fun rescheduleAll()

    suspend fun rescheduleRecurrent()

    suspend fun reschedule(activityId: Long)

    suspend fun cancel(activityId: Long)

    suspend fun cancelAll()

    suspend fun onReminderFired(
        activityId: Long,
        expectedTimerStart: Long,
        expectedTriggerTimestamp: Long,
    )
}