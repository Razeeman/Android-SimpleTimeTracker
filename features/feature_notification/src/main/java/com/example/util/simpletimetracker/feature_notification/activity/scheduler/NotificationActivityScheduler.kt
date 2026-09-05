package com.example.util.simpletimetracker.feature_notification.activity.scheduler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.feature_notification.core.AlarmManagerController
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.net.toUri
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver.Companion.EXTRA_ACTIVITY_REMINDER_ACTIVITY_ID
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver.Companion.EXTRA_ACTIVITY_REMINDER_START
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver.Companion.EXTRA_ACTIVITY_REMINDER_TRIGGER

class NotificationActivityScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManagerController: AlarmManagerController,
) {

    fun schedule(
        activityId: Long,
        timerStartTimestamp: Long,
        triggerTimestamp: Long,
    ) {
        alarmManagerController.scheduleAtTime(
            timestamp = triggerTimestamp,
            pendingIntent = getPendingIntent(
                activityId = activityId,
                timerStartTimestamp = timerStartTimestamp,
                triggerTimestamp = triggerTimestamp,
            ),
        )
    }

    fun cancel(activityId: Long) {
        alarmManagerController.cancelSchedule(
            pendingIntent = getPendingIntent(
                activityId = activityId,
                timerStartTimestamp = 0L,
                triggerTimestamp = 0L,
            ),
        )
    }

    // Cancels schedule with old intent data.
    // TODO remove after several app updates.
    fun cancelLegacyAlarm() {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_ACTIVITY_REMINDER
        }
        alarmManagerController.cancelSchedule(
            PendingIntent.getBroadcast(context, 0, intent, PendingIntents.getFlags()),
        )
    }

    private fun getPendingIntent(
        activityId: Long,
        timerStartTimestamp: Long,
        triggerTimestamp: Long,
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_ACTIVITY_REMINDER
            data = "simpletimetracker://activity-reminder/$activityId".toUri()
            putExtra(EXTRA_ACTIVITY_REMINDER_ACTIVITY_ID, activityId)
            putExtra(EXTRA_ACTIVITY_REMINDER_START, timerStartTimestamp)
            putExtra(EXTRA_ACTIVITY_REMINDER_TRIGGER, triggerTimestamp)
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntents.getFlags(),
        )
    }
}