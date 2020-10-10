package com.example.util.simpletimetracker.feature_notification.recevier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.interactor.NotificationInteractor
import com.example.util.simpletimetracker.feature_notification.di.NotificationComponentProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationInteractor: NotificationInteractor

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action == null) return

        (context.applicationContext as? NotificationComponentProvider)
            ?.notificationComponent
            ?.inject(this)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> onBootCompleted()
        }
    }

    private fun onBootCompleted() {
        GlobalScope.launch {
            notificationInteractor.updateNotifications()
        }
    }
}