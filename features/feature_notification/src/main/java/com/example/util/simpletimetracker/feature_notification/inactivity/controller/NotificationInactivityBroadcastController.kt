package com.example.util.simpletimetracker.feature_notification.inactivity.controller

import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

class NotificationInactivityBroadcastController @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val timeMapper: TimeMapper,
) {

    fun onInactivityReminder() = allowDiskRead { MainScope() }.launch {
        val currentDayOfWeek = Calendar.getInstance()
            .get(Calendar.DAY_OF_WEEK)
            .let(timeMapper::toDayOfWeek)
        if (currentDayOfWeek in prefsInteractor.getInactivityReminderDaysOfWeek()) {
            notificationInactivityInteractor.show()
        }
        checkAndSchedule()
    }

    fun onBootCompleted() = allowDiskRead { MainScope() }.launch {
        checkAndSchedule()
    }

    private suspend fun checkAndSchedule() {
        if (prefsInteractor.getInactivityReminderRecurrent()) {
            notificationInactivityInteractor.checkAndSchedule()
        }
    }
}