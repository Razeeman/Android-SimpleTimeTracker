package com.example.util.simpletimetracker.feature_notification.inactivity.controller

import com.example.util.simpletimetracker.domain.interactor.NotificationInactivityInteractor
import javax.inject.Inject

class NotificationInactivityBroadcastController @Inject constructor(
    private val notificationInactivityInteractor: NotificationInactivityInteractor
) {

    fun onInactivityReminder() {
        notificationInactivityInteractor.show()
    }

    fun onBootCompleted() {
        // TODO reschedule inactivity reminder
    }
}