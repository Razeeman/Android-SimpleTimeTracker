package com.example.util.simpletimetracker.feature_notification.goalTime.scheduler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal.Range
import com.example.util.simpletimetracker.feature_notification.core.AlarmManagerController
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationGoalTimeScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManagerController: AlarmManagerController,
) {

    fun schedule(
        durationMillisFromNow: Long,
        idData: RecordTypeGoal.IdData,
        goalRange: Range,
    ) {
        val timestamp = System.currentTimeMillis() + durationMillisFromNow

        alarmManagerController.scheduleAtTime(
            timestamp = timestamp,
            pendingIntent = getPendingIntent(idData, goalRange),
        )
    }

    fun cancelSchedule(
        idData: RecordTypeGoal.IdData,
        goalRange: Range,
    ) {
        alarmManagerController.cancelSchedule(
            pendingIntent = getPendingIntent(idData, goalRange),
        )
    }

    private fun getPendingIntent(
        idData: RecordTypeGoal.IdData,
        goalRange: Range,
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            when (idData) {
                is RecordTypeGoal.IdData.Type -> {
                    action = when (goalRange) {
                        is Range.Session -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_SESSION
                        is Range.Daily -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_DAILY
                        is Range.Weekly -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_WEEKLY
                        is Range.Monthly -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_MONTHLY
                    }
                    putExtra(NotificationReceiver.EXTRA_GOAL_TIME_TYPE_ID, idData.value)
                }
                is RecordTypeGoal.IdData.Category -> {
                    action = when (goalRange) {
                        is Range.Session -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_CATEGORY_SESSION
                        is Range.Daily -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_CATEGORY_DAILY
                        is Range.Weekly -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_CATEGORY_WEEKLY
                        is Range.Monthly -> NotificationReceiver.ACTION_GOAL_TIME_REMINDER_CATEGORY_MONTHLY
                    }
                    putExtra(NotificationReceiver.EXTRA_GOAL_TIME_CATEGORY_ID, idData.value)
                }
            }
        }

        return PendingIntent.getBroadcast(
            context,
            idData.value.toInt(),
            intent,
            PendingIntents.getFlags(),
        )
    }
}