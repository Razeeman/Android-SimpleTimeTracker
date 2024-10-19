package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.model.WidgetType
import javax.inject.Inject

// TODO check all after actions that need to be done after type delete,
//  also tag, category, record, running record etc.
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
    ) {
        val runningRecordIds = runningRecordInteractor.getAll().map(RunningRecord::id)
        notificationGoalTimeInteractor.cancel(RecordTypeGoal.IdData.Type(typeId))
        notificationGoalTimeInteractor.checkAndReschedule(runningRecordIds + typeId)
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        wearInteractor.update()
    }

    suspend fun onTypeRemoveWithoutArchive() {
        notificationTypeInteractor.updateNotifications()
    }

    suspend fun onTypeArchive() {
        notificationTypeInteractor.updateNotifications()
        wearInteractor.update()
    }

    suspend fun onTypeAddOrChange(
        typeId: Long,
    ) {
        notificationTypeInteractor.updateNotifications()
        notificationGoalTimeInteractor.checkAndReschedule(listOf(typeId))
        widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
        widgetInteractor.updateWidgets(WidgetType.QUICK_SETTINGS)
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        wearInteractor.update()
    }

    suspend fun onDefaultTypesAdd() {
        wearInteractor.update()
    }

    fun onCategoryRemove(
        categoryId: Long,
    ) {
        notificationGoalTimeInteractor.cancel(RecordTypeGoal.IdData.Category(categoryId))
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
    }

    suspend fun onCategoryAddOrChange(
        typeIds: List<Long>,
    ) {
        notificationGoalTimeInteractor.checkAndReschedule(typeIds)
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
    }

    suspend fun onRunningRecordRemove(
        typeId: Long,
        updateWidgets: Boolean,
        updateNotificationSwitch: Boolean,
    ) {
        notificationTypeInteractor.checkAndHide(typeId)
        if (updateNotificationSwitch) {
            notificationActivitySwitchInteractor.updateNotification()
        }
        notificationInactivityInteractor.checkAndSchedule()
        // Cancel if no activity tracked.
        val runningRecordIds = runningRecordInteractor.getAll().map { it.id }
        if (runningRecordIds.isEmpty()) notificationActivityInteractor.cancel()
        notificationGoalTimeInteractor.checkAndReschedule(runningRecordIds + typeId)
        if (updateWidgets) {
            widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
            widgetInteractor.updateWidgets(WidgetType.UNIVERSAL)
            widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
            wearInteractor.update()
        }
    }

    suspend fun onRunningRecordAdd(
        typeId: Long,
        updateNotificationSwitch: Boolean,
    ) {
        notificationTypeInteractor.checkAndShow(typeId)
        if (updateNotificationSwitch) {
            notificationActivitySwitchInteractor.updateNotification()
        }
        notificationInactivityInteractor.cancel()
        // Schedule only on first activity start.
        if (runningRecordInteractor.getAll().size == 1) notificationActivityInteractor.checkAndSchedule()
        notificationGoalTimeInteractor.checkAndReschedule(listOf(typeId))
        widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
        widgetInteractor.updateWidgets(WidgetType.UNIVERSAL)
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        wearInteractor.update()
    }

    suspend fun onRecordRemove(
        typeId: Long,
    ) {
        notificationTypeInteractor.checkAndShow(typeId)
        notificationActivitySwitchInteractor.updateNotification()
        notificationGoalTimeInteractor.checkAndReschedule(listOf(typeId))
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        widgetInteractor.updateSingleWidgets(typeIds = listOf(typeId))
    }

    suspend fun onRecordAddOrChange(
        typeId: Long,
        updateNotificationSwitch: Boolean,
    ) {
        notificationTypeInteractor.checkAndShow(typeId)
        if (updateNotificationSwitch) {
            notificationActivitySwitchInteractor.updateNotification()
        }
        notificationGoalTimeInteractor.checkAndReschedule(listOf(typeId))
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        widgetInteractor.updateSingleWidgets(typeIds = listOf(typeId))
    }

    // Called after record add.
    suspend fun onRecordChangeType(
        originalTypeId: Long,
    ) {
        notificationTypeInteractor.checkAndShow(originalTypeId)
        notificationActivitySwitchInteractor.updateNotification()
        notificationGoalTimeInteractor.checkAndReschedule(listOf(originalTypeId))
    }

    // Called from data edit.
    suspend fun onRecordsChangeType(
        oldTypeIds: Set<Long>,
    ) {
        oldTypeIds.forEach { typeId ->
            notificationGoalTimeInteractor.checkAndReschedule(listOf(typeId))
        }
    }

    suspend fun onTagRemove() {
        wearInteractor.update()
    }

    suspend fun onTagRemoveWithoutArchiving() {
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
    }

    suspend fun onTagAddOrChange() {
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
        wearInteractor.update()
    }

    suspend fun onTagArchive() {
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
        wearInteractor.update()
    }

    suspend fun onGoalTimeReached(
        typeId: Long,
    ) {
        widgetInteractor.updateSingleWidgets(typeIds = listOf(typeId))
        notificationTypeInteractor.checkAndShow(typeId = typeId)
        notificationActivitySwitchInteractor.updateNotification()
    }

    suspend fun onGoalRangeEnd() {
        widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
    }

    suspend fun onRepeatEnabled() {
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
        wearInteractor.update()
    }

    suspend fun onStartOfDaySignChange() {
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
        notificationGoalTimeInteractor.checkAndReschedule()
    }

    suspend fun onStartOfDayChange() {
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
        notificationGoalTimeInteractor.checkAndReschedule()
    }

    suspend fun onUseMilitaryChange() {
        notificationTypeInteractor.updateNotifications()
    }

    suspend fun onUseProportionalMinutesChange() {
        notificationTypeInteractor.updateNotifications()
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
    }

    suspend fun onShowSecondsChange() {
        notificationTypeInteractor.updateNotifications()
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
    }

    suspend fun onFirstDayOfWeekChange() {
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        notificationGoalTimeInteractor.checkAndReschedule()
    }

    fun onShowRecordTagSelectionChange() {
        widgetInteractor.updateWidgets(WidgetType.QUICK_SETTINGS)
    }

    suspend fun onShowTimerNotificationsChange() {
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
    }

    suspend fun onShowTimerNotificationsControlsChange() {
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
    }

    suspend fun onShowNotificationWithSwitchChange() {
        notificationActivitySwitchInteractor.updateNotification()
    }

    suspend fun onInactivityReminderChange() {
        notificationInactivityInteractor.cancel()
        notificationInactivityInteractor.checkAndSchedule()
    }

    suspend fun onActivityReminderChange() {
        notificationActivityInteractor.cancel()
        notificationActivityInteractor.checkAndSchedule()
    }

    // Update all widgets.
    fun onWidgetsTransparencyChange() {
        widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
        widgetInteractor.updateWidgets(WidgetType.UNIVERSAL)
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        widgetInteractor.updateWidgets(WidgetType.QUICK_SETTINGS)
    }

    fun onShowUntrackedInStatisticsChange() {
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
    }

    fun onAllowMultitaskingChange() {
        widgetInteractor.updateWidgets(WidgetType.QUICK_SETTINGS)
    }

    // Update everything.
    suspend fun onBackupRestore() {
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
        notificationGoalTimeInteractor.checkAndReschedule()
        widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
        widgetInteractor.updateWidgets(WidgetType.UNIVERSAL)
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        widgetInteractor.updateWidgets(WidgetType.QUICK_SETTINGS)
        wearInteractor.update()
    }

    suspend fun onCsvImport(
        typeIds: List<Long>,
    ) {
        notificationGoalTimeInteractor.checkAndReschedule(typeIds)
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
    }

    suspend fun onRestoreFromArchive() {
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
        wearInteractor.update()
    }

    // Update everything except goals.
    suspend fun onAppStart() {
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
        widgetInteractor.updateWidgets(WidgetType.RECORD_TYPE)
        widgetInteractor.updateWidgets(WidgetType.UNIVERSAL)
        widgetInteractor.updateWidgets(WidgetType.STATISTICS_CHART)
        widgetInteractor.updateWidgets(WidgetType.QUICK_SETTINGS)
        wearInteractor.update()
    }
}