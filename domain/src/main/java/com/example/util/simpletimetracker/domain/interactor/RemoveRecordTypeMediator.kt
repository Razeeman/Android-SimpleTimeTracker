package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.model.WidgetType
import javax.inject.Inject

class RemoveRecordTypeMediator @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val wearInteractor: WearInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
) {

    suspend fun remove(typeId: Long) {
        recordTypeInteractor.remove(typeId)
        doAfterRemove(typeId)
    }

    private suspend fun doAfterRemove(typeId: Long) {
        val runningRecordIds = runningRecordInteractor.getAll().map(RunningRecord::id)
        notificationGoalTimeInteractor.cancel(RecordTypeGoal.IdData.Type(typeId))
        notificationGoalTimeInteractor.checkAndReschedule(runningRecordIds + typeId)
        widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
        wearInteractor.update()
    }
}