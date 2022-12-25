package com.example.util.simpletimetracker.core.repo

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.example.util.simpletimetracker.domain.extension.orFalse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionRepo @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms().orFalse()
        } else {
            true
        }
    }
}