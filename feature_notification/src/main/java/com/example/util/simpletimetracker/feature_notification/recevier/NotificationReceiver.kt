package com.example.util.simpletimetracker.feature_notification.recevier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.feature_notification.di.NotificationComponentProvider
import com.example.util.simpletimetracker.feature_notification.inactivity.controller.NotificationInactivityBroadcastController
import com.example.util.simpletimetracker.feature_notification.recordType.controller.NotificationTypeBroadcastController
import javax.inject.Inject

class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var typeController: NotificationTypeBroadcastController

    @Inject
    lateinit var inactivityController: NotificationInactivityBroadcastController

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action == null) return

        (context.applicationContext as? NotificationComponentProvider)
            ?.notificationComponent
            ?.inject(this)

        when (intent.action) {
            ACTION_INACTIVITY_REMINDER -> inactivityController.onInactivityReminder()
            Intent.ACTION_BOOT_COMPLETED -> onBootCompleted()
        }
    }

    private fun onBootCompleted() {
        inactivityController.onBootCompleted()
        typeController.onBootCompleted()
    }

    companion object {
        const val ACTION_INACTIVITY_REMINDER =
            "com.example.util.simpletimetracker.ACTION_INACTIVITY_REMINDER"
    }
}