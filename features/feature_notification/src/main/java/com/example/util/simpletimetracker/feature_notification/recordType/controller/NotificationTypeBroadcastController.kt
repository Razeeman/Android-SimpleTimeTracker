package com.example.util.simpletimetracker.feature_notification.recordType.controller

import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.feature_notification.recordType.interactor.ActivityStartStopFromBroadcastInteractor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationTypeBroadcastController @Inject constructor(
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val activityStartStopFromBroadcastInteractor: ActivityStartStopFromBroadcastInteractor,
) {

    fun onActionActivityStart(name: String?) {
        name ?: return
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStart(name)
        }
    }

    fun onActionActivityStop(name: String?) {
        name ?: return
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStop(name)
        }
    }

    fun onBootCompleted() {
        GlobalScope.launch {
            notificationTypeInteractor.updateNotifications()
        }
    }
}