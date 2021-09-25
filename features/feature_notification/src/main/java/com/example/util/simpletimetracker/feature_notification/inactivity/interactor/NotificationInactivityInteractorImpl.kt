package com.example.util.simpletimetracker.feature_notification.inactivity.interactor

import com.example.util.simpletimetracker.core.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_notification.inactivity.manager.NotificationInactivityManager
import com.example.util.simpletimetracker.feature_notification.inactivity.manager.NotificationInactivityParams
import com.example.util.simpletimetracker.feature_notification.inactivity.scheduler.NotificationInactivityScheduler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationInactivityInteractorImpl @Inject constructor(
    private val manager: NotificationInactivityManager,
    private val scheduler: NotificationInactivityScheduler,
    private val prefsInteractor: PrefsInteractor,
    private val runningRecordInteractor: RunningRecordInteractor
) : NotificationInactivityInteractor {

    override suspend fun checkAndSchedule() {
        if (runningRecordInteractor.getAll().isEmpty()) {
            prefsInteractor.getInactivityReminderDuration()
                .takeIf { it > 0 }
                ?.let { it * 1000L }
                ?.let(scheduler::schedule)
        }
    }

    override fun cancel() {
        scheduler.cancelSchedule()
        manager.hide()
    }

    override fun show() {
        GlobalScope.launch {
            NotificationInactivityParams(
                isDarkTheme = prefsInteractor.getDarkMode()
            ).let(manager::show)
        }
    }
}