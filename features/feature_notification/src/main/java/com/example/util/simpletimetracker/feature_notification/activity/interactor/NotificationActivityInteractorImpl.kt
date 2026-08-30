package com.example.util.simpletimetracker.feature_notification.activity.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
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
            .takeIf { runningRecordInteractor.getAll().isNotEmpty() }
            ?.let {
                getDoNotDisturbHandledScheduleInteractor.execute(
                    reminderDurationSeconds = it,
                    dndStart = prefsInteractor.getActivityReminderDoNotDisturbStart(),
                    dndEnd = prefsInteractor.getActivityReminderDoNotDisturbEnd(),
                    activeDaysOfWeek = prefsInteractor.getActivityReminderDaysOfWeek(),
                    nowTimestamp = System.currentTimeMillis(),
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