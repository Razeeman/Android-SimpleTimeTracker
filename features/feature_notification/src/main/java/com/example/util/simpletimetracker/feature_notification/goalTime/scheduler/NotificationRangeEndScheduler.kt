package com.example.util.simpletimetracker.feature_notification.goalTime.scheduler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal.Range
import com.example.util.simpletimetracker.feature_notification.core.AlarmManagerController
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationRangeEndScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManagerController: AlarmManagerController,
) {

    fun schedule(timestamp: Long, goalRange: Range) {
        alarmManagerController.scheduleAtTime(
            timestamp = timestamp,
            pendingIntent = getPendingIntent(goalRange) ?: return,
        )
    }

    fun cancelSchedule(goalRange: Range) {
        alarmManagerController.cancelSchedule(
            pendingIntent = getPendingIntent(goalRange) ?: return,
        )
    }

    private fun getPendingIntent(goalRange: Range): PendingIntent? {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = when (goalRange) {
                is Range.Session -> return null // No need to reschedule.
                is Range.Daily -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_DAY_END
                is Range.Weekly -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_WEEK_END
                is Range.Monthly -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_MONTH_END
            }
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntents.getFlags(),
        )
    }
}