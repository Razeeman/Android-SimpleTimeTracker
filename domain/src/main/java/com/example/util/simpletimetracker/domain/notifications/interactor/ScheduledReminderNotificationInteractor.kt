package com.example.util.simpletimetracker.domain.notifications.interactor

import com.example.util.simpletimetracker.domain.record.model.RecordTimerEvent

interface ScheduledReminderNotificationInteractor {

    suspend fun schedule(reminderId: Long)

    fun cancel(reminderId: Long)

    suspend fun rescheduleAll()

    suspend fun onReminderFired(
        reminderId: Long,
        expectedOccurrenceTimestamp: Long,
    )

    suspend fun onActivityLifecycleEvent(
        event: RecordTimerEvent,
        activityId: Long,
        tagIds: List<Long>,
    )
}
