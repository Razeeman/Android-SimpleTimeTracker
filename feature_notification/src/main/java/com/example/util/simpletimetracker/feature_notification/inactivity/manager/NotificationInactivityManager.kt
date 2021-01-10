package com.example.util.simpletimetracker.feature_notification.inactivity.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.navigation.Router
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationInactivityManager @Inject constructor(
    @AppContext private val context: Context,
    private val router: Router,
    private val resourceRepo: ResourceRepo
) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    fun show() {
        val notification: Notification = buildNotification()
        createAndroidNotificationChannel()
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification)
    }

    fun hide() {
        notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID)
    }

    private fun buildNotification(): Notification {
        val startIntent = router.getMainStartIntent().apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setContentTitle(resourceRepo.getString(R.string.notification_inactivity_title))
            .setContentText(resourceRepo.getString(R.string.notification_inactivity_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun createAndroidNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATIONS_CHANNEL_ID,
                NOTIFICATIONS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val NOTIFICATIONS_CHANNEL_ID = "INACTIVITY"
        private const val NOTIFICATIONS_CHANNEL_NAME = "Inactivity"

        private const val NOTIFICATION_TAG = "inactivity_tag"
        private const val NOTIFICATION_ID = 0
    }
}