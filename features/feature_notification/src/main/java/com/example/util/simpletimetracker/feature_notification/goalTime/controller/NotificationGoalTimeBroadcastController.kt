package com.example.util.simpletimetracker.feature_notification.goalTime.controller

import com.example.util.simpletimetracker.core.interactor.NotificationGoalTimeInteractor
import javax.inject.Inject

class NotificationGoalTimeBroadcastController @Inject constructor(
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor
) {

    fun onGoalTimeReminder(typeId: Long) {
        notificationGoalTimeInteractor.show(typeId)
    }

    fun onBootCompleted() {
        // reschedule inactivity reminder
    }
}