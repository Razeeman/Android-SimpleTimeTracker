package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import javax.inject.Inject

class RemoveRunningRecordMediator @Inject constructor(
    private val runningRecordInteractor: RunningRecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val widgetInteractor: WidgetInteractor
) {

    suspend fun remove(typeId: Long) {
        runningRecordInteractor.remove(typeId)
        notificationTypeInteractor.checkAndHide(typeId)
        notificationInactivityInteractor.checkAndSchedule()
        widgetInteractor.updateWidgets()
    }
}