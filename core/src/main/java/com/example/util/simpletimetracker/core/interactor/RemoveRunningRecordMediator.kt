package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import javax.inject.Inject

class RemoveRunningRecordMediator @Inject constructor(
    private val runningRecordInteractor: RunningRecordInteractor,
    private val notificationInteractor: NotificationInteractor,
    private val widgetInteractor: WidgetInteractor
) {

    suspend fun remove(typeId: Long) {
        runningRecordInteractor.remove(typeId)
        notificationInteractor.hideNotification(typeId)
        widgetInteractor.updateWidgets()
    }
}