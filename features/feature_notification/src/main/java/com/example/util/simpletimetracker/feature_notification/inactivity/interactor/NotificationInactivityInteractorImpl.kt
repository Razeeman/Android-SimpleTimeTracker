package com.example.util.simpletimetracker.feature_notification.inactivity.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.inactivity.manager.NotificationInactivityManager
import com.example.util.simpletimetracker.feature_notification.inactivity.manager.NotificationInactivityParams
import com.example.util.simpletimetracker.feature_notification.inactivity.scheduler.NotificationInactivityScheduler
import javax.inject.Inject

class NotificationInactivityInteractorImpl @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val manager: NotificationInactivityManager,
    private val scheduler: NotificationInactivityScheduler,
    private val prefsInteractor: PrefsInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
) : NotificationInactivityInteractor {

    override suspend fun checkAndSchedule() {
        prefsInteractor.getInactivityReminderDuration()
            .takeIf { it > 0 }
            ?.takeIf { runningRecordInteractor.getAll().isEmpty() }
            ?.let { it * 1000L }
            ?.let(scheduler::schedule)
    }

    override fun cancel() {
        scheduler.cancelSchedule()
        manager.hide()
    }

    override suspend fun show() {
        NotificationInactivityParams(
            title = resourceRepo.getString(R.string.notification_inactivity_title),
            subtitle = resourceRepo.getString(R.string.notification_inactivity_text),
            isDarkTheme = prefsInteractor.getDarkMode()
        ).let(manager::show)
    }
}