package com.example.util.simpletimetracker.feature_notification.pomodoro.controller

import com.example.util.simpletimetracker.domain.interactor.PomodoroCycleNotificationInteractor
import com.example.util.simpletimetracker.feature_notification.pomodoro.interactor.ShowPomodoroNotificationInteractor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationPomodoroBroadcastController @Inject constructor(
    private val showPomodoroNotificationInteractor: ShowPomodoroNotificationInteractor,
    private val pomodoroCycleNotificationInteractor: PomodoroCycleNotificationInteractor,
) {

    fun onReminder(
        cycleType: Long,
    ) = GlobalScope.launch {
        showPomodoroNotificationInteractor.show(cycleType)
        checkAndSchedule()
    }

    fun onBootCompleted() = GlobalScope.launch {
        checkAndSchedule()
    }

    fun onExactAlarmPermissionStateChanged() = GlobalScope.launch {
        checkAndSchedule()
    }

    private suspend fun checkAndSchedule() {
        pomodoroCycleNotificationInteractor.checkAndReschedule()
    }
}