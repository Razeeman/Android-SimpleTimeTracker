package com.example.util.simpletimetracker.feature_notification.activity.controller

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import java.util.Calendar
import javax.inject.Inject

class NotificationActivityBroadcastController @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
    private val timeMapper: TimeMapper,
) {

    suspend fun onActivityReminder() {
        val currentDayOfWeek = Calendar.getInstance()
            .get(Calendar.DAY_OF_WEEK)
            .let(timeMapper::toDayOfWeek)
        if (currentDayOfWeek in prefsInteractor.getActivityReminderDaysOfWeek()) {
            notificationActivityInteractor.show()
        }
        checkAndSchedule()
    }

    suspend fun onBootCompleted() {
        checkAndSchedule()
    }

    private suspend fun checkAndSchedule() {
        if (prefsInteractor.getActivityReminderRecurrent()) {
            notificationActivityInteractor.checkAndSchedule()
        }
    }
}