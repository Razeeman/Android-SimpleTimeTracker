package com.example.util.simpletimetracker.feature_notification.activity.controller

import com.example.util.simpletimetracker.domain.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationActivityBroadcastController @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
) {

    fun onActivityReminder() = GlobalScope.launch {
        notificationActivityInteractor.show()
        checkAndSchedule()
    }

    fun onBootCompleted() = GlobalScope.launch {
        checkAndSchedule()
    }

    private suspend fun checkAndSchedule() {
        if (prefsInteractor.getActivityReminderRecurrent()) {
            notificationActivityInteractor.checkAndSchedule()
        }
    }
}