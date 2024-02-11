package com.example.util.simpletimetracker.feature_notification.activity.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activity.manager.NotificationActivityManager
import com.example.util.simpletimetracker.feature_notification.activity.manager.NotificationActivityParams
import com.example.util.simpletimetracker.feature_notification.activity.scheduler.NotificationActivityScheduler
import com.example.util.simpletimetracker.feature_notification.core.GetDoNotDisturbHandledScheduleInteractor
import javax.inject.Inject

class NotificationActivityInteractorImpl @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val manager: NotificationActivityManager,
    private val scheduler: NotificationActivityScheduler,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getDoNotDisturbHandledScheduleInteractor: GetDoNotDisturbHandledScheduleInteractor,
) : NotificationActivityInteractor {

    override suspend fun checkAndSchedule() {
        prefsInteractor.getActivityReminderDuration()
            .takeIf { it > 0 }
            ?.takeIf { runningRecordInteractor.getAll().isNotEmpty() }
            ?.let { it * 1000L + System.currentTimeMillis() }
            ?.let {
                getDoNotDisturbHandledScheduleInteractor.execute(
                    timestamp = it,
                    dndStart = prefsInteractor.getActivityReminderDoNotDisturbStart(),
                    dndEnd = prefsInteractor.getActivityReminderDoNotDisturbEnd(),
                )
            }
            ?.let(scheduler::schedule)
    }

    override fun cancel() {
        scheduler.cancelSchedule()
        manager.hide()
    }

    override suspend fun show() {
        val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
        val recordNames = runningRecordInteractor.getAll()
            .mapNotNull { recordTypes[it.id] }
            .joinToString(separator = ", ") { it.name }

        NotificationActivityParams(
            title = resourceRepo.getString(R.string.notification_activity_title),
            subtitle = resourceRepo.getString(R.string.notification_activity_text, recordNames),
            isDarkTheme = prefsInteractor.getDarkMode(),
        ).let(manager::show)
    }
}