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

class NotificationRangeEndScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManagerController: AlarmManagerController,
) {

    fun schedule(timestamp: Long, goalTimeType: GoalTimeType) {
        alarmManagerController.scheduleAtTime(timestamp, getPendingIntent(goalTimeType) ?: return)
    }

    fun cancelSchedule(goalTimeType: GoalTimeType) {
        alarmManagerController.cancelSchedule(getPendingIntent(goalTimeType) ?: return)
    }

    private fun getPendingIntent(goalTimeType: GoalTimeType): PendingIntent? {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = when (goalTimeType) {
                is GoalTimeType.Session -> return null // No need to reschedule.
                is GoalTimeType.Day -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_DAY_END
                is GoalTimeType.Week -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_WEEK_END
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