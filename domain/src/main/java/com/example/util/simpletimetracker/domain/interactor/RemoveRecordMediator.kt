package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.WidgetType
import javax.inject.Inject

class RemoveRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor,
) {

    suspend fun remove(recordId: Long, typeId: Long) {
        recordInteractor.remove(recordId)
        doAfterRemove(typeId)
    }

    suspend fun doAfterRemove(typeId: Long) {
        notificationTypeInteractor.checkAndShow(typeId)
        notificationGoalTimeInteractor.checkAndReschedule(listOf(typeId))
        widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
        widgetInteractor.updateSingleWidgets(typeIds = listOf(typeId))
    }
}