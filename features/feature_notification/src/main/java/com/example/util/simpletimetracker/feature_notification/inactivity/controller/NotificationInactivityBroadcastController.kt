package com.example.util.simpletimetracker.feature_notification.inactivity.controller

import com.example.util.simpletimetracker.domain.interactor.NotificationInactivityInteractor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationInactivityBroadcastController @Inject constructor(
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
) {

    fun onInactivityReminder() = GlobalScope.launch {
        notificationInactivityInteractor.show()
        notificationInactivityInteractor.checkAndSchedule()
    }

    fun onBootCompleted() = GlobalScope.launch {
        notificationInactivityInteractor.checkAndSchedule()
    }
}