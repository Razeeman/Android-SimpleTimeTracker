package com.example.util.simpletimetracker.feature_notification.core

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.util.simpletimetracker.domain.extension.orFalse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmManagerController @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun cancelSchedule(pendingIntent: PendingIntent) {
        alarmManager?.cancel(pendingIntent)
    }

    fun scheduleAtTime(timestamp: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager?.canScheduleExactAlarms().orFalse()) {
                scheduleExact(timestamp, pendingIntent)
            } else {
                scheduleInexact(timestamp, pendingIntent)
            }
        } else {
            scheduleExact(timestamp, pendingIntent)
        }
    }

    private fun scheduleExact(timestamp: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager?.setExactAndAllowWhileIdle(RTC_WAKEUP, timestamp, pendingIntent)
        } else {
            alarmManager?.setExact(RTC_WAKEUP, timestamp, pendingIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun scheduleInexact(timestamp: Long, pendingIntent: PendingIntent) {
        alarmManager?.setAndAllowWhileIdle(RTC_WAKEUP, timestamp, pendingIntent)
    }
}