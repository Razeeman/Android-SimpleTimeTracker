package com.example.util.simpletimetracker.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.manager.NotificationParams
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.ui.MainActivity
import javax.inject.Inject

class NotificationManagerImpl @Inject constructor(
    @AppContext private val context: Context
) : com.example.util.simpletimetracker.core.manager.NotificationManager {

    private val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)

    override fun show(params: NotificationParams) {
        val notification: Notification = buildNotification(params)
        createAndroidNotificationChannel()
        notificationManager.notify(params.id, notification)
    }

    override fun hide(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildNotification(params: NotificationParams): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_24px)
            .setContentTitle("Simple Time Tracker")
            .setContentText(params.text)
            .setContentIntent(contentIntent)
        // TODO not closable
        return builder.build()
    }

    private fun createAndroidNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDERS_CHANNEL_ID,
                REMINDERS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val REMINDERS_CHANNEL_ID = "REMINDERS"
        private const val REMINDERS_CHANNEL_NAME = "Notifications"
    }
}