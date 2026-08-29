package com.example.util.simpletimetracker.feature_notification.scheduledReminder.scheduler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.feature_notification.core.AlarmManagerController
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.net.toUri

class ScheduledReminderAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManagerController: AlarmManagerController,
) {

    fun schedule(
        reminderId: Long,
        triggerTimestamp: Long,
        expectedOccurrenceTimestamp: Long,
    ) {
        alarmManagerController.scheduleAtTime(
            timestamp = triggerTimestamp,
            pendingIntent = getPendingIntent(
                reminderId = reminderId,
                expectedOccurrenceTimestamp = expectedOccurrenceTimestamp,
            ),
        )
    }

    fun cancel(reminderId: Long) {
        alarmManagerController.cancelSchedule(
            pendingIntent = getPendingIntent(
                reminderId = reminderId,
                expectedOccurrenceTimestamp = 0L,
            ),
        )
    }

    private fun getPendingIntent(
        reminderId: Long,
        expectedOccurrenceTimestamp: Long,
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_SCHEDULED_REMINDER
            data = getUniqueIntentData(reminderId).toUri()
            putExtra(
                NotificationReceiver.EXTRA_SCHEDULED_REMINDER_ID,
                reminderId,
            )
            putExtra(
                NotificationReceiver.EXTRA_SCHEDULED_REMINDER_EXPECTED_TIMESTAMP,
                expectedOccurrenceTimestamp,
            )
        }
        return PendingIntent.getBroadcast(
            context,
            0, // Intents are unique because of data.
            intent,
            PendingIntents.getFlags(),
        )
    }

    // Gives each reminder alarm a unique stable Intent.data URI.
    // This is needed because Android identifies PendingIntents using fields such as action,
    // component, and data but not extras.
    // Since every alarm uses request code 0, the unique URI prevents reminders from overwriting each other.
    // It also lets cancel(reminderId) recreate and cancel the exact same PendingIntent, even though
    // its timestamp extra differs.
    private fun getUniqueIntentData(reminderId: Long): String {
        return "simpletimetracker://scheduled-reminder/$reminderId"
    }
}