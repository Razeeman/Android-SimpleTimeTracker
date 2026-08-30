package com.example.util.simpletimetracker.feature_notification.activity.interactor

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activityReminder.interactor.ActivityReminderRuleResolver
import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.activityReminder.repo.ActivityReminderOverrideRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.extension.toDomainDayOfWeek
import com.example.util.simpletimetracker.domain.extension.toLocalDateTime
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activity.manager.NotificationActivityManager
import com.example.util.simpletimetracker.feature_notification.activity.manager.NotificationActivityParams
import com.example.util.simpletimetracker.feature_notification.activity.scheduler.NotificationActivityScheduler
import com.example.util.simpletimetracker.feature_notification.core.GetDoNotDisturbHandledScheduleInteractor
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class NotificationActivityInteractorImpl @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val manager: NotificationActivityManager,
    private val scheduler: NotificationActivityScheduler,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val activityReminderOverrideRepo: ActivityReminderOverrideRepo,
    private val ruleResolver: ActivityReminderRuleResolver,
    private val getDoNotDisturbHandledScheduleInteractor: GetDoNotDisturbHandledScheduleInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
) : NotificationActivityInteractor {

    // Serializes notification work for each activity ID. It prevents races between operations such as:
    // - an alarm firing while the timer is being stopped,
    // - a rule change rescheduling while an old alarm is delivered,
    // - two concurrent reschedules installing conflicting alarms.
    private val activityLocks = mutableMapOf<Long, Mutex>()

    override suspend fun rescheduleAll() {
        scheduler.cancelLegacyAlarm()

        val runningRecords = runningRecordInteractor.getAll()
        val runningIds = runningRecords.map(RunningRecord::id).toSet()
        val knownIds = listOf(
            recordTypeInteractor.getAll().map(RecordType::id),
            activityReminderOverrideRepo.getAll().map(ActivityReminderOverride::activityId),
            runningIds,
        ).flatten().toSet()

        knownIds.filterNot { it in runningIds }.forEach { cancel(it) }

        runningRecords.forEach { runningRecord ->
            withActivityLock(runningRecord.id) {
                rescheduleLocked(
                    runningRecord = runningRecord,
                    nowTimestamp = currentTimestampProvider.get(),
                )
            }
        }
    }

    override suspend fun reschedule(activityId: Long) = withActivityLock(activityId) {
        val runningRecord = runningRecordInteractor.get(activityId)
        if (runningRecord == null) {
            cancelLocked(activityId)
        } else {
            rescheduleLocked(
                runningRecord = runningRecord,
                nowTimestamp = currentTimestampProvider.get(),
            )
        }
    }

    override suspend fun cancel(activityId: Long) = withActivityLock(activityId) {
        cancelLocked(activityId)
    }

    override suspend fun cancelAll() {
        scheduler.cancelLegacyAlarm()
        val ids = listOf(
            recordTypeInteractor.getAll().map(RecordType::id),
            activityReminderOverrideRepo.getAll().map(ActivityReminderOverride::activityId),
            runningRecordInteractor.getAll().map(RunningRecord::id),
        ).flatten().toSet()
        ids.forEach { cancel(it) }
    }

    override suspend fun onReminderFired(
        activityId: Long,
        expectedTimerStart: Long,
        expectedTriggerTimestamp: Long,
    ) = withActivityLock(activityId) {
        if (expectedTriggerTimestamp <= 0L) return@withActivityLock
        val runningRecord = runningRecordInteractor.get(activityId)
        val recordType = recordTypeInteractor.get(activityId)
        val rule = ruleResolver.resolve(activityId)
        if (
            runningRecord == null ||
            recordType == null ||
            rule == null ||
            runningRecord.timeStarted != expectedTimerStart
        ) {
            cancelLocked(activityId)
            return@withActivityLock
        }

        val nowTimestamp = currentTimestampProvider.get()
        if (nowTimestamp < expectedTriggerTimestamp) {
            scheduler.schedule(
                activityId = activityId,
                timerStartTimestamp = runningRecord.timeStarted,
                triggerTimestamp = expectedTriggerTimestamp,
            )
            return@withActivityLock
        }

        // Check in case date was changed due to system settings change.
        val currentDayOfWeek = nowTimestamp
            .toLocalDateTime(TimeZone.getDefault())
            .dayOfWeek.toDomainDayOfWeek()
        if (currentDayOfWeek in rule.applicableDaysOfWeek) {
            val isDarkTheme = prefsInteractor.getDarkMode()
            NotificationActivityParams(
                activityId = activityId,
                title = resourceRepo.getString(R.string.notification_activity_title),
                subtitle = resourceRepo.getString(
                    R.string.notification_activity_text,
                    recordType.name,
                ),
                icon = iconMapper.mapIcon(recordType.icon),
                color = colorMapper.mapToColorInt(recordType.color, isDarkTheme),
            ).let(manager::show)
        }

        if (rule.recurrent) {
            scheduleNextLocked(
                runningRecord = runningRecord,
                schedulingTimestamp = nowTimestamp,
                rule = rule,
            )
        } else {
            scheduler.cancel(activityId)
        }
    }

    private suspend fun rescheduleLocked(
        runningRecord: RunningRecord,
        nowTimestamp: Long,
    ) {
        val activityId = runningRecord.id
        scheduler.cancel(activityId)
        val rule = ruleResolver.resolve(activityId)
        val recordTypeExists = recordTypeInteractor.get(activityId) != null
        if (rule == null || !recordTypeExists) {
            manager.hide(activityId)
            return
        }
        scheduleNextLocked(
            runningRecord = runningRecord,
            schedulingTimestamp = nowTimestamp,
            rule = rule,
        )
    }

    private fun scheduleNextLocked(
        runningRecord: RunningRecord,
        schedulingTimestamp: Long,
        rule: ActivityReminderOverride.Rule,
    ) {
        scheduler.cancel(runningRecord.id)
        val triggerTimestamp = getDoNotDisturbHandledScheduleInteractor.execute(
            reminderDurationSeconds = rule.durationSeconds,
            dndStart = rule.doNotDisturbStartMillis,
            dndEnd = rule.doNotDisturbEndMillis,
            activeDaysOfWeek = rule.applicableDaysOfWeek,
            nowTimestamp = schedulingTimestamp,
        ) ?: return
        scheduler.schedule(
            activityId = runningRecord.id,
            timerStartTimestamp = runningRecord.timeStarted,
            triggerTimestamp = triggerTimestamp,
        )
    }

    private fun cancelLocked(activityId: Long) {
        scheduler.cancel(activityId)
        manager.hide(activityId)
    }

    private suspend fun <T> withActivityLock(
        activityId: Long,
        action: suspend () -> T,
    ) {
        if (activityId <= 0L) return
        val mutex = synchronized(activityLocks) {
            activityLocks.getOrPut(activityId) { Mutex() }
        }
        return mutex.withLock { action() }
    }
}