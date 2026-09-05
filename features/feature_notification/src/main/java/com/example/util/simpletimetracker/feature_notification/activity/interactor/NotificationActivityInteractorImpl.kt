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
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivityInteractor.Companion.SHARED_REMINDER_ID
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
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
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

    // Membership changes and alarm delivery must observe one consistent inherited group.
    private val reminderMutex = Mutex()

    override suspend fun onActivityStarted(activityId: Long) = reminderMutex.withLock {
        val runningRecord = runningRecordInteractor.get(activityId) ?: return@withLock
        when (val mode = activityReminderOverrideRepo.get(activityId)?.mode) {
            null -> {
                cancelActivitySpecific(activityId)
                val inheritedRecords = getInheritedRunningRecords()
                // Schedule only on first activity start.
                if (inheritedRecords.size == 1) {
                    rescheduleShared(
                        inheritedRecords = inheritedRecords,
                        nowTimestamp = currentTimestampProvider.get(),
                    )
                }
            }
            is ActivityReminderOverride.Mode.Custom -> {
                rescheduleCustom(
                    runningRecord = runningRecord,
                    rule = mode.rule,
                    nowTimestamp = currentTimestampProvider.get(),
                )
            }
            is ActivityReminderOverride.Mode.Disabled -> {
                cancelActivitySpecific(activityId)
            }
        }
    }

    override suspend fun onActivityStopped(activityId: Long) = reminderMutex.withLock {
        cancelActivitySpecific(activityId)
        // Custom and disabled activities cannot change the shared group when they stop.
        if (activityReminderOverrideRepo.get(activityId) == null &&
            // Cancel if no activity tracked.
            getInheritedRunningRecords().isEmpty()
        ) {
            cancelShared()
        }
    }

    override suspend fun onReminderOverrideChanged(
        activityId: Long,
        wasInherited: Boolean,
    ) = reminderMutex.withLock {
        val runningRecord = runningRecordInteractor.get(activityId)
        val currentMode = activityReminderOverrideRepo.get(activityId)?.mode
        val isInherited = currentMode == null

        cancelActivitySpecific(activityId)
        if (runningRecord != null && currentMode is ActivityReminderOverride.Mode.Custom) {
            rescheduleCustom(
                runningRecord = runningRecord,
                rule = currentMode.rule,
                nowTimestamp = currentTimestampProvider.get(),
            )
        }

        // Activity was changed from global to custom settings or vice versa.
        if (wasInherited == isInherited || runningRecord == null) return@withLock
        val inheritedRecords = getInheritedRunningRecords()
        when {
            // Was global, now custom - cancel old reminder, there will be new one with new settings.
            wasInherited && inheritedRecords.isEmpty() -> cancelShared()
            // Was custom, now global - reschedule with new settings.
            isInherited && inheritedRecords.size == 1 -> rescheduleShared(
                inheritedRecords = inheritedRecords,
                nowTimestamp = currentTimestampProvider.get(),
            )
        }
    }

    override suspend fun rescheduleDefault() = reminderMutex.withLock {
        scheduler.cancelLegacyAlarm()

        // Global setting changes affect only activities that inherit the default rule.
        // Custom reminders keep their existing schedule and cadence.
        cancelShared()
        val inheritedRecords = getInheritedRunningRecords()
        if (inheritedRecords.isNotEmpty()) {
            rescheduleShared(
                inheritedRecords = inheritedRecords,
                nowTimestamp = currentTimestampProvider.get(),
            )
        }
    }

    // Lifecycle recovery must not replay one-shot reminders. Any existing alarm is left alone
    // on app start and naturally absent after reboot, restore, or package replacement.
    override suspend fun rescheduleRecurrent() = reminderMutex.withLock {
        scheduler.cancelLegacyAlarm()

        val runningRecords = runningRecordInteractor.getAll()
        val runningIds = runningRecords.map(RunningRecord::id).toSet()
        val overrides = activityReminderOverrideRepo.getAll()
            .associateBy(ActivityReminderOverride::activityId)
        cancelNotRunning(runningIds, overrides.keys)

        // Custom recurrent.
        runningRecords.forEach { runningRecord ->
            when (val mode = overrides[runningRecord.id]?.mode) {
                null -> cancelActivitySpecific(runningRecord.id)
                is ActivityReminderOverride.Mode.Disabled -> {
                    cancelActivitySpecific(runningRecord.id)
                }
                is ActivityReminderOverride.Mode.Custom -> if (mode.rule.recurrent) {
                    rescheduleCustom(
                        runningRecord = runningRecord,
                        rule = mode.rule,
                        nowTimestamp = currentTimestampProvider.get(),
                    )
                }
            }
        }

        // Global recurrent.
        val inheritedRecords = runningRecords.filter { it.id !in overrides }
        val defaultRule = ruleResolver.resolveDefault()
        when {
            inheritedRecords.isEmpty() || defaultRule == null -> cancelShared()
            defaultRule.recurrent -> rescheduleShared(
                inheritedRecords = inheritedRecords,
                nowTimestamp = currentTimestampProvider.get(),
            )
            else -> Unit
        }
    }

    override suspend fun cancelAll() = reminderMutex.withLock {
        scheduler.cancelLegacyAlarm()
        val ids = listOf(
            recordTypeInteractor.getAll().map(RecordType::id),
            activityReminderOverrideRepo.getAll().map(ActivityReminderOverride::activityId),
            runningRecordInteractor.getAll().map(RunningRecord::id),
        ).flatten().toSet()
        ids.forEach(::cancelActivitySpecific)
        cancelShared()
    }

    override suspend fun onReminderFired(
        activityId: Long,
        expectedTimerStart: Long,
        expectedTriggerTimestamp: Long,
    ) = reminderMutex.withLock {
        if (expectedTriggerTimestamp <= 0L) return@withLock
        if (activityId == SHARED_REMINDER_ID) {
            onSharedReminderFired(
                expectedTimerStart = expectedTimerStart,
                expectedTriggerTimestamp = expectedTriggerTimestamp,
            )
        } else {
            onCustomReminderFired(
                activityId = activityId,
                expectedTimerStart = expectedTimerStart,
                expectedTriggerTimestamp = expectedTriggerTimestamp,
            )
        }
    }

    private suspend fun onCustomReminderFired(
        activityId: Long,
        expectedTimerStart: Long,
        expectedTriggerTimestamp: Long,
    ) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val runningRecord = runningRecordInteractor.get(activityId)
        val recordType = recordTypeInteractor.get(activityId)
        // Old activity-specific alarms must not fall back to the global rule.
        val rule = (activityReminderOverrideRepo.get(activityId)?.mode as?
            ActivityReminderOverride.Mode.Custom)?.rule
        if (
            runningRecord == null ||
            recordType == null ||
            rule == null ||
            runningRecord.timeStarted != expectedTimerStart
        ) {
            cancelActivitySpecific(activityId)
            return
        }

        onReminderFiredInternal(
            activityId = activityId,
            timerStartTimestamp = runningRecord.timeStarted,
            expectedTriggerTimestamp = expectedTriggerTimestamp,
            rule = rule,
            subtitle = {
                resourceRepo.getString(
                    R.string.notification_activity_text,
                    recordType.name,
                )
            },
            icon = iconMapper.mapIcon(recordType.icon),
            color = colorMapper.mapToColorInt(recordType.color, isDarkTheme),
        )
    }

    private suspend fun onSharedReminderFired(
        expectedTimerStart: Long,
        expectedTriggerTimestamp: Long,
    ) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val inheritedRecords = getInheritedRunningRecords()
        val rule = ruleResolver.resolveDefault()
        if (inheritedRecords.isEmpty() || rule == null) {
            cancelShared()
            return
        }

        onReminderFiredInternal(
            activityId = SHARED_REMINDER_ID,
            timerStartTimestamp = expectedTimerStart,
            expectedTriggerTimestamp = expectedTriggerTimestamp,
            rule = rule,
            subtitle = {
                val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
                val activityNames = inheritedRecords.mapNotNull { recordTypes[it.id]?.name }
                resourceRepo.getString(
                    R.string.notification_activity_text,
                    activityNames.joinToString(separator = ", "),
                )
            },
            icon = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        )
    }

    private suspend fun onReminderFiredInternal(
        activityId: Long,
        timerStartTimestamp: Long,
        expectedTriggerTimestamp: Long,
        rule: ActivityReminderOverride.Rule,
        subtitle: suspend () -> String,
        icon: RecordTypeIcon,
        color: Int,
    ) {
        // In case clock was changed just when broadcast is handled, rare but would lose reminder.
        val nowTimestamp = currentTimestampProvider.get()
        if (nowTimestamp < expectedTriggerTimestamp) {
            scheduler.schedule(
                activityId = activityId,
                timerStartTimestamp = timerStartTimestamp,
                triggerTimestamp = expectedTriggerTimestamp,
            )
            return
        }

        val currentDayOfWeek = nowTimestamp
            .toLocalDateTime(TimeZone.getDefault())
            .dayOfWeek.toDomainDayOfWeek()
        // Check that the day is still correct, time could be changed just when broadcast is handled.
        if (currentDayOfWeek in rule.applicableDaysOfWeek) {
            NotificationActivityParams(
                activityId = activityId,
                title = resourceRepo.getString(R.string.notification_activity_title),
                subtitle = subtitle(),
                icon = icon,
                color = color,
            ).let(manager::show)
        }

        if (rule.recurrent) {
            scheduleNext(
                reminderId = activityId,
                timerStartTimestamp = timerStartTimestamp,
                schedulingTimestamp = nowTimestamp,
                rule = rule,
            )
        } else {
            scheduler.cancel(activityId)
        }
    }

    private suspend fun rescheduleCustom(
        runningRecord: RunningRecord,
        rule: ActivityReminderOverride.Rule,
        nowTimestamp: Long,
    ) {
        val activityId = runningRecord.id
        scheduler.cancel(activityId)
        if (recordTypeInteractor.get(activityId) == null) {
            manager.hide(activityId)
            return
        }
        scheduleNext(
            reminderId = activityId,
            timerStartTimestamp = runningRecord.timeStarted,
            schedulingTimestamp = nowTimestamp,
            rule = rule,
        )
    }

    private suspend fun rescheduleShared(
        inheritedRecords: List<RunningRecord>,
        nowTimestamp: Long,
    ) {
        scheduler.cancel(SHARED_REMINDER_ID)
        val resolvedRule = ruleResolver.resolveDefault()
        if (resolvedRule == null) {
            manager.hide(SHARED_REMINDER_ID)
            return
        }
        scheduleNext(
            reminderId = SHARED_REMINDER_ID,
            timerStartTimestamp = inheritedRecords.minOf(RunningRecord::timeStarted),
            schedulingTimestamp = nowTimestamp,
            rule = resolvedRule,
        )
    }

    private fun scheduleNext(
        reminderId: Long,
        timerStartTimestamp: Long,
        schedulingTimestamp: Long,
        rule: ActivityReminderOverride.Rule,
    ) {
        scheduler.cancel(reminderId)
        val triggerTimestamp = getDoNotDisturbHandledScheduleInteractor.execute(
            reminderDurationSeconds = rule.durationSeconds,
            dndStart = rule.doNotDisturbStartMillis,
            dndEnd = rule.doNotDisturbEndMillis,
            activeDaysOfWeek = rule.applicableDaysOfWeek,
            nowTimestamp = schedulingTimestamp,
        ) ?: return
        scheduler.schedule(
            activityId = reminderId,
            timerStartTimestamp = timerStartTimestamp,
            triggerTimestamp = triggerTimestamp,
        )
    }

    private fun cancelActivitySpecific(activityId: Long) {
        scheduler.cancel(activityId)
        manager.hide(activityId)
    }

    private fun cancelShared() {
        scheduler.cancel(SHARED_REMINDER_ID)
        manager.hide(SHARED_REMINDER_ID)
    }

    private suspend fun getInheritedRunningRecords(): List<RunningRecord> {
        val overriddenIds = activityReminderOverrideRepo.getAll()
            .map(ActivityReminderOverride::activityId)
            .toSet()
        return runningRecordInteractor.getAll().filter { it.id !in overriddenIds }
    }

    private suspend fun cancelNotRunning(
        runningIds: Set<Long>,
        overrideIds: Set<Long>,
    ) {
        val knownIds = recordTypeInteractor.getAll().map(RecordType::id).toSet() + overrideIds
        knownIds.filterNot { it in runningIds }.forEach(::cancelActivitySpecific)
    }
}