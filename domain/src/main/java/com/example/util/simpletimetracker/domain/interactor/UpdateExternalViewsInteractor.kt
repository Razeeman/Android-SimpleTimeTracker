package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.model.WidgetType
import javax.inject.Inject

class UpdateExternalViewsInteractor @Inject constructor(
    private val runningRecordInteractor: RunningRecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationActivitySwitchInteractor: NotificationActivitySwitchInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val wearInteractor: WearInteractor,
) {

    // Also removes running records and records.
    // Categories are affected.
    suspend fun onTypeRemove(
        typeId: Long,
        fromArchive: Boolean,
    ) {
        val runningRecordIds = runningRecordInteractor.getAll().map(RunningRecord::id)

        runUpdates(
            Update.GoalCancel(RecordTypeGoal.IdData.Type(typeId)),
            Update.GoalReschedule(runningRecordIds + typeId),
            Update.WidgetStatistics,
            Update.WidgetSingleType(typeId),
            Update.Wear.takeIf { !fromArchive },
            Update.NotificationTypes.takeIf { !fromArchive },
            Update.NotificationWithControls.takeIf { !fromArchive }
        )
    }

    suspend fun onTypeArchive() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.Wear,
        )
    }

    suspend fun onTypeAddOrChange(
        typeId: Long,
    ) {
        runUpdates(
            Update.NotificationTypes,
            Update.GoalReschedule(listOf(typeId)),
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
            Update.WidgetStatistics,
            Update.Wear,
        )
    }

    suspend fun onDefaultTypesAdd() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.Wear,
        )
    }

    suspend fun onCategoryRemove(
        categoryId: Long,
    ) {
        runUpdates(
            Update.GoalCancel(RecordTypeGoal.IdData.Category(categoryId)),
            Update.WidgetStatistics,
        )
    }

    suspend fun onCategoryAddOrChange(
        typeIds: List<Long>,
    ) {
        runUpdates(
            Update.GoalReschedule(typeIds),
            Update.WidgetStatistics,
        )
    }

    suspend fun onRunningRecordRemove(
        typeId: Long,
        updateWidgets: Boolean,
        updateNotificationSwitch: Boolean,
    ) {
        val runningRecordIds = runningRecordInteractor.getAll().map { it.id }

        runUpdates(
            Update.NotificationTypeHide(typeId),
            Update.NotificationWithControls.takeIf { updateNotificationSwitch },
            Update.InactivityReminderReschedule,
            Update.ActivityReminderCancel.takeIf {
                // Cancel if no activity tracked.
                runningRecordIds.isEmpty()
            },
            Update.GoalReschedule(runningRecordIds + typeId),
            Update.WidgetSingleTypes.takeIf { updateWidgets },
            Update.WidgetUniversal.takeIf { updateWidgets },
            Update.WidgetStatistics.takeIf { updateWidgets },
            Update.Wear.takeIf { updateWidgets },
        )
    }

    suspend fun onRunningRecordAdd(
        typeId: Long,
        updateNotificationSwitch: Boolean,
    ) {
        runUpdates(
            Update.NotificationType(typeId),
            Update.NotificationWithControls.takeIf { updateNotificationSwitch },
            Update.InactivityReminderCancel,
            Update.ActivityReminderReschedule.takeIf {
                // Schedule only on first activity start.
                runningRecordInteractor.getAll().size == 1
            },
            Update.GoalReschedule(listOf(typeId)),
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
            Update.WidgetStatistics,
            Update.Wear,
        )
    }

    suspend fun onRecordRemove(
        typeId: Long,
    ) {
        runUpdates(
            Update.NotificationType(typeId),
            Update.NotificationWithControls,
            Update.GoalReschedule(listOf(typeId)),
            Update.WidgetStatistics,
            Update.WidgetSingleType(typeId),
        )
    }

    suspend fun onRecordAddOrChange(
        typeId: Long,
        updateNotificationSwitch: Boolean,
    ) {
        runUpdates(
            Update.NotificationType(typeId),
            Update.NotificationWithControls.takeIf { updateNotificationSwitch },
            Update.GoalReschedule(listOf(typeId)),
            Update.WidgetStatistics,
            Update.WidgetSingleType(typeId),
        )
    }

    // Called after record add.
    suspend fun onRecordChangeType(
        originalTypeId: Long,
    ) {
        runUpdates(
            Update.NotificationType(originalTypeId),
            Update.GoalReschedule(listOf(originalTypeId)),
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
        fromArchive: Boolean,
    ) {
        runUpdates(
            Update.NotificationTypes.takeIf { !fromArchive },
            Update.NotificationWithControls.takeIf { !fromArchive },
            Update.Wear,
        )
    }

    suspend fun onTagAddOrChange() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
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

    suspend fun onGoalTimeReached(
        typeId: Long,
    ) {
        runUpdates(
            Update.WidgetSingleType(typeId),
            Update.NotificationType(typeId),
            Update.NotificationWithControls,
        )
    }

    suspend fun onGoalRangeEnd() {
        runUpdates(
            Update.WidgetSingleTypes,
            Update.NotificationTypes,
            Update.NotificationWithControls,
        )
    }

    suspend fun onRepeatEnabled() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.Wear,
        )
    }

    suspend fun onStartOfDaySignChange() {
        runUpdates(
            Update.WidgetStatistics,
            Update.WidgetSingleTypes,
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.GoalReschedule(),
        )
    }

    suspend fun onStartOfDayChange() {
        runUpdates(
            Update.WidgetStatistics,
            Update.WidgetSingleTypes,
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.GoalReschedule(),
        )
    }

    suspend fun onUseMilitaryChange() {
        runUpdates(
            Update.NotificationTypes,
        )
    }

    suspend fun onUseProportionalMinutesChange() {
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

    suspend fun onShowNotificationWithSwitchChange() {
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
            Update.ActivityReminderCancel,
            Update.ActivityReminderReschedule,
        )
    }

    // Update all widgets.
    suspend fun onWidgetsTransparencyChange() {
        runUpdates(
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
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
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
            Update.WidgetStatistics,
            Update.WidgetQuickSettings,
            Update.Wear,
        )
    }

    suspend fun onCsvImport() {
        val runningRecordIds = runningRecordInteractor.getAll().map { it.id }

        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.GoalReschedule(runningRecordIds),
            Update.WidgetStatistics,
            Update.WidgetSingleTypes,
        )
    }

    suspend fun onRestoreFromArchive() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.Wear,
        )
    }

    // Update everything except goals.
    suspend fun onAppStart() {
        runUpdates(
            Update.NotificationTypes,
            Update.NotificationWithControls,
            Update.WidgetSingleTypes,
            Update.WidgetUniversal,
            Update.WidgetStatistics,
            Update.WidgetQuickSettings,
            Update.Wear,
        )
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
                notificationTypeInteractor.checkAndShow(update.typeId)
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
            is Update.WidgetSingleTypes -> {
                widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
            }
            is Update.WidgetSingleType -> {
                widgetInteractor.updateSingleWidgets(typeIds = listOf(update.typeId))
            }
            is Update.Wear -> {
                wearInteractor.update()
            }
            is Update.GoalReschedule -> {
                notificationGoalTimeInteractor.checkAndReschedule(update.typeIds)
            }
            is Update.GoalCancel -> {
                notificationGoalTimeInteractor.cancel(update.idData)
            }
            is Update.ActivityReminderCancel -> {
                notificationActivityInteractor.cancel()
            }
            is Update.ActivityReminderReschedule -> {
                notificationActivityInteractor.checkAndSchedule()
            }
            is Update.InactivityReminderCancel -> {
                notificationInactivityInteractor.cancel()
            }
            is Update.InactivityReminderReschedule -> {
                notificationInactivityInteractor.checkAndSchedule()
            }
        }
    }

    private sealed interface Update {
        object NotificationTypes : Update
        data class NotificationType(val typeId: Long) : Update
        data class NotificationTypeHide(val typeId: Long) : Update
        object NotificationWithControls : Update
        object WidgetStatistics : Update
        object WidgetQuickSettings : Update
        object WidgetUniversal : Update
        object WidgetSingleTypes : Update
        data class WidgetSingleType(val typeId: Long) : Update
        object Wear : Update
        data class GoalReschedule(val typeIds: List<Long> = emptyList()) : Update
        data class GoalCancel(val idData: RecordTypeGoal.IdData) : Update
        object ActivityReminderCancel : Update
        object ActivityReminderReschedule : Update
        object InactivityReminderCancel : Update
        object InactivityReminderReschedule : Update
    }
}