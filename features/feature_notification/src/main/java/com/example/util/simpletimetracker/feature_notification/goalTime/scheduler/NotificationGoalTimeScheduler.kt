package com.example.util.simpletimetracker.feature_notification.goalTime.scheduler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
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
        goal: RecordTypeGoal,
    ) {
        val timestamp = System.currentTimeMillis() + durationMillisFromNow

        alarmManagerController.scheduleAtTime(
            timestamp = timestamp,
            pendingIntent = getPendingIntent(goal.id),
        )
    }

    fun cancelSchedule(goalId: Long) {
        alarmManagerController.cancelSchedule(
            pendingIntent = getPendingIntent(goalId),
        )
    }

    private fun getPendingIntent(goalId: Long): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_GOAL_TIME_REMINDER
            data = "simpletimetracker://goal-time/$goalId".toUri()
            putExtra(NotificationReceiver.EXTRA_GOAL_ID, goalId)
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntents.getFlags(),
        )
    }
}