package com.example.util.simpletimetracker.feature_notification.inactivity.interactor

import com.example.util.simpletimetracker.core.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_notification.inactivity.manager.NotificationInactivityManager
import com.example.util.simpletimetracker.feature_notification.inactivity.scheduler.NotificationInactivityScheduler
import javax.inject.Inject

class NotificationInactivityInteractorImpl @Inject constructor(
    private val manager: NotificationInactivityManager,
    private val scheduler: NotificationInactivityScheduler,
    private val runningRecordInteractor: RunningRecordInteractor
) : NotificationInactivityInteractor {

    override suspend fun checkAndSchedule() {
        if (runningRecordInteractor.getAll().isEmpty()) {
            scheduler.schedule()
        }
    }

    override fun cancel() {
        scheduler.cancelSchedule()
        manager.hide()
    }

    override fun show() {
        manager.show()
    }
}