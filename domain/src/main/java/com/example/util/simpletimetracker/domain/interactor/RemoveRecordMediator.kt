package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.WidgetType
import javax.inject.Inject

class RemoveRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor,
) {

    suspend fun remove(recordId: Long, typeId: Long) {
        recordInteractor.remove(recordId)
        doAfterRemove(typeId)
    }

    private suspend fun doAfterRemove(typeId: Long) {
        notificationGoalTimeInteractor.checkAndReschedule(typeId)
        widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
    }
}