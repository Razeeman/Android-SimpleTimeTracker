package com.example.util.simpletimetracker.feature_notification.scheduledReminder.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledReminderNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val router: Router,
) {

    private val notificationManager = NotificationManagerCompat.from(context)

    fun show(reminderId: Long, text: String) {
        createNotificationChannel()
        if (!notificationManager.areNotificationsEnabled()) return

        val startIntent = router.getMainStartIntent().apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            startIntent,
            PendingIntents.getFlags(),
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        notificationManager.notify(notificationTag(reminderId), NOTIFICATION_ID, notification)
    }

    fun hide(reminderId: Long) {
        notificationManager.cancel(notificationTag(reminderId), NOTIFICATION_ID)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                null,
            )
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "SCHEDULED_REMINDERS"
        const val CHANNEL_NAME = "Scheduled reminders"
        private const val NOTIFICATION_ID = 0

        fun notificationTag(reminderId: Long): String {
            return "scheduled_reminder_$reminderId"
        }
    }
}