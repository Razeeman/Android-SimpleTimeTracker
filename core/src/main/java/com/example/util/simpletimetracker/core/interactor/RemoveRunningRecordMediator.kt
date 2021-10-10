package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class RemoveRunningRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor,
) {

    suspend fun removeWithRecordAdd(runningRecord: RunningRecord) {
        recordInteractor.add(
            typeId = runningRecord.id,
            timeStarted = runningRecord.timeStarted,
            comment = runningRecord.comment,
            tagIds = runningRecord.tagIds,
        )
        remove(runningRecord.id)
    }

    suspend fun remove(typeId: Long) {
        runningRecordInteractor.remove(typeId)
        notificationTypeInteractor.checkAndHide(typeId)
        notificationInactivityInteractor.checkAndSchedule()
        notificationGoalTimeInteractor.cancel(typeId)
        widgetInteractor.updateWidgets()
    }
}