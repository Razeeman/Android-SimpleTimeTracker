package com.example.util.simpletimetracker.feature_notification.pomodoro.scheduler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.model.PomodoroCycleType
import com.example.util.simpletimetracker.feature_notification.core.AlarmManagerController
import com.example.util.simpletimetracker.feature_notification.pomodoro.mapper.NotificationPomodoroMapper
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationPomodoroScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManagerController: AlarmManagerController,
    private val notificationPomodoroMapper: NotificationPomodoroMapper,
) {

    // TODO refactor schedulers, lots of duplication
    fun schedule(
        timestamp: Long,
        cycleType: PomodoroCycleType,
    ) {
        val intent = getPendingIntent(cycleType)
        alarmManagerController.scheduleAtTime(timestamp, intent)
    }

    fun cancelSchedule() {
        // Cycle doesn't matter here.
        val intent = getPendingIntent(PomodoroCycleType.Focus)
        alarmManagerController.cancelSchedule(intent)
    }

    private fun getPendingIntent(
        cycleType: PomodoroCycleType,
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_POMODORO_REMINDER
            putExtra(
                NotificationReceiver.EXTRA_POMODORO_CYCLE_TYPE,
                notificationPomodoroMapper.mapCycleType(cycleType),
            )
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntents.getFlags(),
        )
    }
}