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

class NotificationGoalTimeScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManagerController: AlarmManagerController,
) {

    fun schedule(durationMillisFromNow: Long, typeId: Long, goalRange: Range) {
        val timestamp = System.currentTimeMillis() + durationMillisFromNow

        alarmManagerController.scheduleAtTime(
            timestamp = timestamp,
            pendingIntent = getPendingIntent(typeId, goalRange),
        )
    }

    fun cancelSchedule(typeId: Long, goalRange: Range) {
        alarmManagerController.cancelSchedule(
            pendingIntent = getPendingIntent(typeId, goalRange),
        )
    }

    private fun getPendingIntent(typeId: Long, goalRange: Range): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = when (goalRange) {
                is Range.Session -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_SESSION
                is Range.Daily -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_DAILY
                is Range.Weekly -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_WEEKLY
                is Range.Monthly -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_MONTHLY
            }
            putExtra(NotificationReceiver.EXTRA_GOAL_TIME_TYPE_ID, typeId)
        }

        return PendingIntent.getBroadcast(
            context,
            typeId.toInt(),
            intent,
            PendingIntents.getFlags(),
        )
    }
}