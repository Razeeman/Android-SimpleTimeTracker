package com.example.util.simpletimetracker.feature_notification.pomodoro.controller

import com.example.util.simpletimetracker.domain.pomodoro.interactor.PomodoroCycleNotificationInteractor
import com.example.util.simpletimetracker.feature_notification.pomodoro.interactor.ShowPomodoroNotificationInteractor
import javax.inject.Inject

class NotificationPomodoroBroadcastController @Inject constructor(
    private val showPomodoroNotificationInteractor: ShowPomodoroNotificationInteractor,
    private val pomodoroCycleNotificationInteractor: PomodoroCycleNotificationInteractor,
) {

    suspend fun onReminder(
        cycleType: Long,
    ) {
        showPomodoroNotificationInteractor.show(cycleType)
        checkAndSchedule()
    }

    suspend fun onBootCompleted() {
        checkAndSchedule()
    }

    suspend fun onExactAlarmPermissionStateChanged() {
        checkAndSchedule()
    }

    private suspend fun checkAndSchedule() {
        pomodoroCycleNotificationInteractor.checkAndReschedule()
    }
}