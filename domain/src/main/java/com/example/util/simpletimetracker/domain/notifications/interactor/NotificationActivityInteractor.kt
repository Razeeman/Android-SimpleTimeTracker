package com.example.util.simpletimetracker.domain.notifications.interactor

interface NotificationActivityInteractor {

    suspend fun onActivityStarted(activityId: Long)

    suspend fun onActivityStopped(activityId: Long)

    suspend fun onReminderOverrideChanged(activityId: Long, wasInherited: Boolean)

    suspend fun rescheduleDefault()

    suspend fun rescheduleRecurrent()

    suspend fun cancelAll()

    suspend fun onReminderFired(
        activityId: Long,
        expectedTimerStart: Long,
        expectedTriggerTimestamp: Long,
    )

    companion object {
        // Runtime-only identity used by the shared reminder for activities that inherit
        // the global rule. Activity IDs are positive, so this cannot collide with one.
        const val SHARED_REMINDER_ID: Long = Long.MIN_VALUE
    }
}