package com.example.util.simpletimetracker.feature_notification.scheduledReminder.interactor

import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.notifications.interactor.ScheduledReminderNotificationInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderOccurrenceCalculator
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledRemindersDataUpdateInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.scheduledReminder.repo.ScheduledReminderRepo
import com.example.util.simpletimetracker.feature_notification.scheduledReminder.manager.ScheduledReminderNotificationManager
import com.example.util.simpletimetracker.feature_notification.scheduledReminder.scheduler.ScheduledReminderAlarmScheduler
import com.example.util.simpletimetracker.feature_notification.scheduledReminder.utils.ScheduledReminderConditionEvaluator
import java.util.TimeZone
import javax.inject.Inject

class ScheduledReminderNotificationInteractorImpl @Inject constructor(
    private val repo: ScheduledReminderRepo,
    private val occurrenceCalculator: ScheduledReminderOccurrenceCalculator,
    private val conditionEvaluator: ScheduledReminderConditionEvaluator,
    private val scheduler: ScheduledReminderAlarmScheduler,
    private val manager: ScheduledReminderNotificationManager,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val scheduledRemindersDataUpdateInteractor: ScheduledRemindersDataUpdateInteractor,
) : ScheduledReminderNotificationInteractor {

    override suspend fun schedule(reminderId: Long) {
        scheduler.cancel(reminderId)
        val reminder = repo.get(reminderId)?.takeIf { it.enabled } ?: return

        val occurrence = occurrenceCalculator.calculateNext(
            schedule = reminder.schedule,
            nowTimestamp = currentTimestampProvider.get(),
            timeZone = TimeZone.getDefault(),
            catchUpOverdueOneTime = true,
        ) ?: return

        scheduler.schedule(
            reminderId = reminder.id,
            triggerTimestamp = occurrence.triggerTimestamp,
            expectedOccurrenceTimestamp = occurrence.expectedOccurrenceTimestamp,
        )
    }

    override fun cancel(reminderId: Long) {
        scheduler.cancel(reminderId)
        manager.hide(reminderId)
    }

    override suspend fun rescheduleAll() {
        repo.getAll().forEach { reminder ->
            if (reminder.enabled) {
                schedule(reminder.id)
            } else {
                cancel(reminder.id)
            }
        }
    }

    override suspend fun onReminderFired(
        reminderId: Long,
        expectedOccurrenceTimestamp: Long,
    ) {
        val reminder = repo.get(reminderId)?.takeIf { it.enabled } ?: return
        val isExpected = occurrenceCalculator.matchesExpectedOccurrence(
            schedule = reminder.schedule,
            expectedOccurrenceTimestamp = expectedOccurrenceTimestamp,
            timeZone = TimeZone.getDefault(),
        )
        val nowTimestamp = currentTimestampProvider.get()
        if (!isExpected || nowTimestamp < expectedOccurrenceTimestamp) {
            schedule(reminderId)
            return
        }

        if (conditionEvaluator.shouldShow(reminder.condition)) {
            manager.show(
                reminderId = reminder.id,
                text = reminder.text,
            )
        }

        when (reminder.schedule) {
            is ScheduledReminder.Schedule.OneTime -> {
                scheduler.cancel(reminder.id)
                repo.setEnabled(id = reminder.id, enabled = false)
                scheduledRemindersDataUpdateInteractor.send()
            }
            is ScheduledReminder.Schedule.Weekly,
            is ScheduledReminder.Schedule.Monthly,
            -> schedule(reminder.id)
        }
    }
}