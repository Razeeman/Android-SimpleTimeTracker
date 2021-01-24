package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class AddRunningRecordMediator @Inject constructor(
    private val runningRecordInteractor: RunningRecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor
) {

    suspend fun add(typeId: Long, timeStarted: Long? = null) {
        if (runningRecordInteractor.get(typeId) == null) {
            RunningRecord(
                id = typeId,
                timeStarted = timeStarted ?: System.currentTimeMillis()
            ).let {
                runningRecordInteractor.add(it)
                notificationTypeInteractor.checkAndShow(typeId)
                notificationInactivityInteractor.cancel()
                notificationGoalTimeInteractor.checkAndReschedule(typeId)
                widgetInteractor.updateWidgets()
            }
        }
    }
}