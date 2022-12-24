package com.example.util.simpletimetracker.feature_notification.goalTime.scheduler

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationGoalTimeScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun schedule(durationMillisFromNow: Long, typeId: Long, goalTimeType: GoalTimeType) {
        val timestamp = System.currentTimeMillis() + durationMillisFromNow

        scheduleAtTime(timestamp, typeId, goalTimeType)
    }

    fun cancelSchedule(typeId: Long, goalTimeType: GoalTimeType) {
        alarmManager?.cancel(getPendingIntent(typeId, goalTimeType))
    }

    private fun scheduleAtTime(timestamp: Long, typeId: Long, goalTimeType: GoalTimeType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager?.setAndAllowWhileIdle(RTC_WAKEUP, timestamp, getPendingIntent(typeId, goalTimeType))
        } else {
            alarmManager?.set(RTC_WAKEUP, timestamp, getPendingIntent(typeId, goalTimeType))
        }
    }

    private fun getPendingIntent(typeId: Long, goalTimeType: GoalTimeType): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = when (goalTimeType) {
                is GoalTimeType.Session -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_SESSION
                is GoalTimeType.Day -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_DAILY
                is GoalTimeType.Week -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_WEEKLY
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