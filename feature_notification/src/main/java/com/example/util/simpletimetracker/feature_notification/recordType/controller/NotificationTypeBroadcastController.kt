package com.example.util.simpletimetracker.feature_notification.recordType.controller

import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationTypeBroadcastController @Inject constructor(
    private val notificationTypeInteractor: NotificationTypeInteractor
) {

    fun onBootCompleted() {
        GlobalScope.launch {
            notificationTypeInteractor.updateNotifications()
        }
    }
}