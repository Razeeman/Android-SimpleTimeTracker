package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RunningRecord
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RemoveRunningRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun removeWithRecordAdd(runningRecord: RunningRecord) {
        val durationToIgnore = prefsInteractor.getIgnoreShortRecordsDuration()
        val duration = TimeUnit.MILLISECONDS
            .toSeconds(System.currentTimeMillis() - runningRecord.timeStarted)

        if (duration > durationToIgnore || durationToIgnore == 0L) {
            // No need to update widgets and notification because it will be done in running record remove.
            recordInteractor.add(
                typeId = runningRecord.id,
                timeStarted = runningRecord.timeStarted,
                comment = runningRecord.comment,
                tagIds = runningRecord.tagIds,
            )
        }
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