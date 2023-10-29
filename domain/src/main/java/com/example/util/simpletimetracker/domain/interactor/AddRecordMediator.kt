package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.WidgetType
import javax.inject.Inject

class AddRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor,
) {

    suspend fun add(
        record: Record,
    ) {
        recordInteractor.add(record)
        doAfterAdd(record.typeId)
    }

    suspend fun doAfterAdd(typeId: Long) {
        notificationTypeInteractor.checkAndShow(typeId)
        notificationGoalTimeInteractor.checkAndReschedule(typeId)
        widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
        widgetInteractor.updateSingleWidgets(typeIds = listOf(typeId))
    }
}