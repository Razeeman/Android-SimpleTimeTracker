package com.example.util.simpletimetracker.domain.notifications.interactor

import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.wear.WearInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.widget.model.WidgetType
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordTimerEvent
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import javax.inject.Inject

class UpdateExternalViewsInteractor @Inject constructor(
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationActivitySwitchInteractor: NotificationActivitySwitchInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val scheduledReminderNotificationInteractor: ScheduledReminderNotificationInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val wearInteractor: WearInteractor,
    private val prefsInteractor: PrefsInteractor,
) {

    // Also removes running records and records.
    // Categories are affected.
    // Activity reminder is not called intentionally, because it is called from running record remove
    // which is called just before this type remove call.
    suspend fun onTypeRemove(
        typeId: Long,
        fromArchive: Boolean,
        removedGoalIds: List<Long>,
    ) {
        val runningRecordIds = runningRecordInteractor.getAll().map(RunningRecord::id)

        runUpdates(
            Update.GoalCancelByIds(removedGoalIds),
            Update.GoalCancelByOwners(listOf(RecordTypeGoal.IdData.Type(typeId))),
            Update.GoalReschedule(runningRecordIds + typeId),
            Update.GoalTagReschedule(),
            Update.WidgetStatistics,
            Update.WidgetSingleTypes.takeIf { getRetroactiveTrackingMode() }
                ?: Update.WidgetSingleType(listOf(typeId)),
            Update.WidgetUniversal.takeIf { getRetroactiveTrackingMode() },
            Update.WidgetGrid.takeIf { !fromArchive },
            Update.Wear.takeIf { !fromArchive || getRetroactiveTrackingMode() },
            Update.NotificationTypes.takeIf { !fromArchive },
            Update.NotificationWithControls.takeIf { !fromArchive },
        )
    }

    suspend fun onTypeArchive() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.Wear,
            Update.WidgetGrid,
        )
    }

    suspend fun onTypeAddOrChange(
        typeId: Long,
        initialCategories: Set<Long>,
        removedCategories: Set<Long>,
        removedGoalIds: List<Long>,
    ) {
        val remainingTypeIds = removedCategories
            .flatMap { recordTypeCategoryInteractor.getTypes(it) }

        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            // Category goals will be rescheduled after this.
            Update.GoalCancelByIds(removedGoalIds),
            Update.GoalCancelByOwners(initialCategories.map(RecordTypeGoal.IdData::Category)),
            // Goals changed, or categories assigned changed.
            Update.GoalReschedule((remainingTypeIds + typeId).distinct()),
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
            Update.WidgetGrid,
            Update.WidgetStatistics,
            Update.Wear,
        )
    }

    suspend fun onDefaultTypesAdd() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.WidgetGrid,
            Update.Wear,
        )
    }

    suspend fun onCategoryRemove(
        categoryId: Long,
        removedGoalIds: List<Long>,
    ) {
        runUpdates(
            Update.GoalCancelByIds(removedGoalIds),
            Update.GoalCancelByOwners(listOf(RecordTypeGoal.IdData.Category(categoryId))),
            Update.WidgetStatistics,
        )
    }

    suspend fun onCategoryAddOrChange(
        categoryId: Long,
        typeIds: List<Long>,
        removedGoalIds: List<Long>,
    ) {
        runUpdates(
            Update.GoalCancelByIds(removedGoalIds),
            Update.GoalCancelByOwners(listOf(RecordTypeGoal.IdData.Category(categoryId))),
            Update.GoalReschedule(typeIds), // Goals changed, or activities assigned changed.
            Update.WidgetStatistics,
        )
    }

    suspend fun onRunningRecordRemove(
        typeId: Long,
        tagIds: List<Long>,
        updateWidgets: Boolean,
        updateNotificationSwitch: Boolean,
        lifecycleEvent: RecordTimerEvent?,
    ) {
        val runningRecords = runningRecordInteractor.getAll()
        val runningRecordIds = runningRecords.map(RunningRecord::id)
        val runningRecordTagIds = runningRecords.flatMap(RunningRecord::tags)
            .map(RecordBase.Tag::tagId)
        val fullTagIds = runningRecordTagIds + tagIds

        runUpdates(
            Update.NotificationTypeHide(typeId),
            Update.NotificationWithControls.takeIf { updateNotificationSwitch },
            Update.InactivityReminderReschedule,
            Update.ActivityReminderStopped(typeId),
            Update.GoalReschedule(runningRecordIds + typeId),
            Update.GoalTagReschedule(fullTagIds).takeIf { fullTagIds.isNotEmpty() },
            Update.WidgetSingleTypes.takeIf { updateWidgets },
            Update.WidgetUniversal.takeIf { updateWidgets },
            Update.WidgetGrid.takeIf { updateWidgets },
            Update.WidgetStatistics.takeIf { updateWidgets },
            Update.Wear.takeIf { updateWidgets },
            lifecycleEvent?.let {
                Update.ScheduledReminderLifecycleEvent(
                    typeId = typeId,
                    tagIds = tagIds,
                    event = lifecycleEvent,
                )
            },
        )
    }

    suspend fun onRunningRecordAdd(
        typeId: Long,
        tagIds: List<Long>,
        updateNotificationSwitch: Boolean,
        lifecycleEvent: RecordTimerEvent?,
    ) {
        runUpdates(
            Update.NotificationType(listOf(typeId)),
            Update.NotificationWithControls.takeIf { updateNotificationSwitch },
            Update.InactivityReminderCancel,
            Update.ActivityReminderStarted(typeId),
            Update.GoalReschedule(listOf(typeId)),
            Update.GoalTagReschedule(tagIds).takeIf { tagIds.isNotEmpty() },
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
            Update.WidgetGrid,
            Update.WidgetStatistics,
            Update.Wear,
            lifecycleEvent?.let {
                Update.ScheduledReminderLifecycleEvent(
                    typeId = typeId,
                    tagIds = tagIds,
                    event = lifecycleEvent,
                )
            },
        )
    }

    suspend fun onRecordRemove(
        typeIds: List<Long>,
        tagIds: List<Long>,
    ) {
        runUpdates(
            Update.NotificationType(typeIds),
            Update.NotificationWithControls,
            Update.GoalReschedule(typeIds),
            Update.GoalTagReschedule(tagIds).takeIf { tagIds.isNotEmpty() },
            Update.WidgetStatistics,
            Update.WidgetSingleTypes.takeIf { getRetroactiveTrackingMode() }
                ?: Update.WidgetSingleType(typeIds),
            Update.WidgetUniversal.takeIf { getRetroactiveTrackingMode() },
            Update.WidgetGrid,
            Update.Wear.takeIf { getRetroactiveTrackingMode() },
        )
    }

    suspend fun onRecordAddOrChange(
        typeIds: List<Long>,
        tagIds: List<Long>,
        updateNotificationSwitch: Boolean,
    ) {
        runUpdates(
            Update.NotificationType(typeIds),
            Update.NotificationWithControls.takeIf { updateNotificationSwitch },
            Update.GoalReschedule(typeIds),
            Update.GoalTagReschedule(tagIds).takeIf { tagIds.isNotEmpty() },
            Update.WidgetStatistics,
            Update.WidgetSingleTypes.takeIf { getRetroactiveTrackingMode() }
                ?: Update.WidgetSingleType(typeIds),
            Update.WidgetUniversal.takeIf { getRetroactiveTrackingMode() },
            Update.WidgetGrid,
            Update.Wear.takeIf { getRetroactiveTrackingMode() },
        )
    }

    suspend fun onRecordTimeEndedChange(
        typeIds: List<Long>,
        tagIds: List<Long>,
    ) {
        runUpdates(
            Update.GoalReschedule(typeIds),
            Update.GoalTagReschedule(tagIds).takeIf { tagIds.isNotEmpty() },
        )
    }

    suspend fun onInstantRecordAdd() {
        runUpdates(
            Update.Wear,
        )
    }

    // Called after record add.
    suspend fun onRecordChangeType(
        originalTypeIds: List<Long>,
    ) {
        runUpdates(
            Update.NotificationType(originalTypeIds),
            Update.GoalReschedule(originalTypeIds),
        )
    }

    // Called after record add.
    suspend fun onRecordChangeTags(
        originalTagIds: List<Long>,
    ) {
        runUpdates(
            Update.GoalTagReschedule(originalTagIds).takeIf { originalTagIds.isNotEmpty() },
        )
    }

    // Called from data edit.
    suspend fun onRecordsChangeType(
        oldTypeIds: Set<Long>,
    ) {
        runUpdates(
            Update.GoalReschedule(oldTypeIds.toList()),
        )
    }

    suspend fun onTagRemove(
        tagId: Long,
        fromArchive: Boolean,
        removedGoalIds: List<Long>,
    ) {
        runUpdates(
            Update.NotificationTypes.takeIf { !fromArchive },
            Update.NotificationWithControls.takeIf { !fromArchive },
            Update.GoalCancelByIds(removedGoalIds),
            Update.GoalCancelByOwners(listOf(RecordTypeGoal.IdData.Tag(tagId))),
            Update.Wear,
        )
    }

    suspend fun onTagAddOrChange(
        tagId: Long,
        removedGoalIds: List<Long>,
    ) {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.GoalCancelByIds(removedGoalIds),
            Update.GoalCancelByOwners(listOf(RecordTypeGoal.IdData.Tag(tagId))),
            Update.GoalTagReschedule(listOf(tagId)), // Goals changed.
            Update.Wear,
        )
    }

    suspend fun onTagArchive() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.Wear,
        )
    }

    suspend fun onActivitySuggestionsChanged() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.Wear,
        )
    }

    suspend fun onGoalTimeReached(
        typeId: Long,
    ) {
        runUpdates(
            Update.WidgetSingleType(listOf(typeId)),
            Update.NotificationType(listOf(typeId)),
            Update.NotificationWithControls,
            Update.WidgetGrid,
        )
    }

    suspend fun onGoalRangeEnd() {
        runUpdates(
            Update.WidgetSingleTypes,
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.WidgetGrid,
        )
    }

    suspend fun onRepeatEnabled() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.WidgetGrid,
            Update.Wear,
        )
    }

    suspend fun onStartOfDaySignChange() {
        runUpdates(
            Update.WidgetStatistics,
            Update.WidgetSingleTypes,
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.WidgetGrid,
            Update.GoalReschedule(),
            Update.GoalTagReschedule(),
        )
    }

    suspend fun onStartOfDayChange() {
        runUpdates(
            Update.WidgetStatistics,
            Update.WidgetSingleTypes,
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.WidgetGrid,
            Update.GoalReschedule(),
            Update.GoalTagReschedule(),
        )
    }

    suspend fun onUseMilitaryChange() {
        runUpdates(
            Update.NotificationTypes,
        )
    }

    suspend fun onDurationFormatChange() {
        runUpdates(
            Update.NotificationTypes,
            Update.WidgetStatistics,
        )
    }

    suspend fun onShowSecondsChange() {
        runUpdates(
            Update.NotificationTypes,
            Update.WidgetStatistics,
        )
    }

    suspend fun onFirstDayOfWeekChange() {
        runUpdates(
            Update.WidgetStatistics,
            Update.GoalReschedule(),
            Update.GoalTagReschedule(),
        )
    }

    suspend fun onShowRecordTagSelectionChange() {
        runUpdates(
            Update.WidgetQuickSettings,
        )
    }

    suspend fun onShowTimerNotificationsChange() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
        )
    }

    suspend fun onShowTimerNotificationsControlsChange() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
        )
    }

    suspend fun onShowNotificationsEvenWithNoTimersChange() {
        runUpdates(
            Update.NotificationWithControls,
        )
    }

    suspend fun onInactivityReminderChange() {
        runUpdates(
            Update.InactivityReminderCancel,
            Update.InactivityReminderReschedule,
        )
    }

    suspend fun onActivityReminderChange() {
        runUpdates(
            Update.ActivityReminderRescheduleDefault,
        )
    }

    suspend fun onRetroactiveTrackingModeChange() {
        runUpdates(
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
            Update.WidgetGrid,
            Update.NotificationWithControls,
            Update.Wear,
        )
    }

    // Update all widgets.
    suspend fun onWidgetsTransparencyChange() {
        runUpdates(
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
            Update.WidgetGrid,
            Update.WidgetStatistics,
            Update.WidgetQuickSettings,
        )
    }

    suspend fun onShowUntrackedInStatisticsChange() {
        runUpdates(
            Update.WidgetStatistics,
        )
    }

    suspend fun onAllowMultitaskingChange() {
        runUpdates(
            Update.WidgetQuickSettings,
        )
    }

    // Update everything.
    suspend fun onBackupRestore() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.GoalReschedule(),
            Update.GoalTagReschedule(),
            Update.ScheduledReminderReschedule,
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
            Update.WidgetGrid,
            Update.WidgetStatistics,
            Update.WidgetQuickSettings,
            Update.Wear,
        )
    }

    suspend fun onCsvImport() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.GoalReschedule(),
            Update.GoalTagReschedule(),
            Update.WidgetStatistics,
            Update.WidgetSingleTypes,
            Update.WidgetGrid,
        )
    }

    suspend fun onRestoreFromArchive() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.WidgetGrid,
            Update.Wear,
        )
    }

    // Update everything except goals (consuming).
    // Reminders rescheduled in order to handle exact alarm permission being revoked.
    // Activity reminders are not rescheduled in order to not lose current timer,
    // need to reschedule only on actual state change.
    // TODO Goals and Pomodoro need equivalent exact alarm revocation recovery.
    //  Persist the last observed permission state and fully recover them only when it changes.
    suspend fun onAppStart() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.ScheduledReminderReschedule,
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
            Update.WidgetGrid,
            Update.WidgetStatistics,
            Update.WidgetQuickSettings,
            Update.Wear,
        )
    }

    private suspend fun getRetroactiveTrackingMode(): Boolean {
        return prefsInteractor.getRetroactiveTrackingMode()
    }

    private suspend fun runUpdates(vararg updates: Update?) {
        updates.filterNotNull().forEach { runUpdate(it) }
    }

    private suspend fun runUpdate(update: Update) {
        when (update) {
            is Update.NotificationTypes -> {
                notificationTypeInteractor.updateNotifications()
            }
            is Update.NotificationType -> {
                update.typeIds.forEach { notificationTypeInteractor.checkAndShow(it) }
            }
            is Update.NotificationTypeHide -> {
                notificationTypeInteractor.checkAndHide(update.typeId)
            }
            is Update.NotificationWithControls -> {
                notificationActivitySwitchInteractor.updateNotification()
            }
            is Update.WidgetStatistics -> {
                widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
            }
            is Update.WidgetQuickSettings -> {
                widgetInteractor.updateWidgets(WidgetType.QUICK_SETTINGS)
            }
            is Update.WidgetUniversal -> {
                widgetInteractor.updateWidgets(WidgetType.UNIVERSAL)
            }
            is Update.WidgetGrid -> {
                widgetInteractor.updateWidgets(WidgetType.GRID)
            }
            is Update.WidgetSingleTypes -> {
                widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
            }
            is Update.WidgetSingleType -> {
                widgetInteractor.updateSingleWidgets(typeIds = update.typeIds)
            }
            is Update.Wear -> {
                wearInteractor.update()
            }
            is Update.GoalReschedule -> {
                notificationGoalTimeInteractor.checkAndReschedule(update.typeIds)
            }
            is Update.GoalTagReschedule -> {
                notificationGoalTimeInteractor.checkAndRescheduleTags(update.tagIds)
            }
            is Update.GoalCancelByIds -> {
                notificationGoalTimeInteractor.cancel(update.goalIds)
            }
            is Update.GoalCancelByOwners -> {
                update.owners.forEach { notificationGoalTimeInteractor.cancel(it) }
            }
            is Update.ActivityReminderStarted -> {
                notificationActivityInteractor.onActivityStarted(update.activityId)
            }
            is Update.ActivityReminderStopped -> {
                notificationActivityInteractor.onActivityStopped(update.activityId)
            }
            is Update.ActivityReminderRescheduleDefault -> {
                notificationActivityInteractor.rescheduleDefault()
            }
            is Update.InactivityReminderCancel -> {
                notificationInactivityInteractor.cancel()
            }
            is Update.InactivityReminderReschedule -> {
                notificationInactivityInteractor.checkAndSchedule()
            }
            is Update.ScheduledReminderReschedule -> {
                scheduledReminderNotificationInteractor.rescheduleAll()
            }
            is Update.ScheduledReminderLifecycleEvent -> {
                scheduledReminderNotificationInteractor.onActivityLifecycleEvent(
                    event = update.event,
                    activityId = update.typeId,
                    tagIds = update.tagIds,
                )
            }
        }
    }

    private sealed interface Update {
        data object NotificationTypes : Update
        data class NotificationType(val typeIds: List<Long>) : Update
        data class NotificationTypeHide(val typeId: Long) : Update
        data object NotificationWithControls : Update
        data object WidgetStatistics : Update
        data object WidgetQuickSettings : Update
        data object WidgetUniversal : Update
        data object WidgetGrid : Update
        data object WidgetSingleTypes : Update
        data class WidgetSingleType(val typeIds: List<Long>) : Update
        data object Wear : Update
        data class GoalReschedule(val typeIds: List<Long> = emptyList()) : Update
        data class GoalTagReschedule(val tagIds: List<Long> = emptyList()) : Update
        data class GoalCancelByIds(val goalIds: List<Long>) : Update
        data class GoalCancelByOwners(val owners: List<RecordTypeGoal.IdData>) : Update
        data class ActivityReminderStarted(val activityId: Long) : Update
        data class ActivityReminderStopped(val activityId: Long) : Update
        data object ActivityReminderRescheduleDefault : Update
        data object InactivityReminderCancel : Update
        data object InactivityReminderReschedule : Update
        data object ScheduledReminderReschedule : Update
        data class ScheduledReminderLifecycleEvent(
            val typeId: Long,
            val tagIds: List<Long>,
            val event: RecordTimerEvent,
        ) : Update
    }
}