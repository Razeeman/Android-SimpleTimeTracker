package com.example.util.simpletimetracker.feature_notification.goalTime.scheduler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.feature_notification.core.AlarmManagerController
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationGoalTimeScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManagerController: AlarmManagerController,
) {

    fun schedule(durationMillisFromNow: Long, typeId: Long, goalTimeType: GoalTimeType) {
        val timestamp = System.currentTimeMillis() + durationMillisFromNow
        alarmManagerController.scheduleAtTime(timestamp, getPendingIntent(typeId, goalTimeType))
    }

    fun cancelSchedule(typeId: Long, goalTimeType: GoalTimeType) {
        alarmManagerController.cancelSchedule(getPendingIntent(typeId, goalTimeType))
    }

    private fun getPendingIntent(typeId: Long, goalTimeType: GoalTimeType): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = when (goalTimeType) {
                is GoalTimeType.Session -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_SESSION
                is GoalTimeType.Day -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_DAILY
                is GoalTimeType.Week -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_WEEKLY
                is GoalTimeType.Month -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_MONTHLY
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