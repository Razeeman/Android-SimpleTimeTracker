package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.WidgetType
import javax.inject.Inject

class AddRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationActivitySwitchInteractor: NotificationActivitySwitchInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor,
) {

    suspend fun add(
        record: Record,
        updateNotificationSwitch: Boolean = true,
    ) {
        recordInteractor.add(record)
        doAfterAdd(
            typeId = record.typeId,
            updateNotificationSwitch = updateNotificationSwitch,
        )
    }

    suspend fun doAfterAdd(
        typeId: Long,
        updateNotificationSwitch: Boolean = true,
    ) {
        notificationTypeInteractor.checkAndShow(typeId)
        if (updateNotificationSwitch) {
            notificationActivitySwitchInteractor.updateNotification()
        }
        notificationGoalTimeInteractor.checkAndReschedule(listOf(typeId))
        widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
        widgetInteractor.updateSingleWidgets(typeIds = listOf(typeId))
    }
}