package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RunningRecord
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RemoveRunningRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val activityStartedStoppedBroadcastInteractor: ActivityStartedStoppedBroadcastInteractor,
    private val pomodoroStopInteractor: PomodoroStopInteractor,
    private val updateExternalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun removeWithRecordAdd(
        runningRecord: RunningRecord,
        updateWidgets: Boolean = true,
        updateNotificationSwitch: Boolean = true,
    ) {
        val durationToIgnore = prefsInteractor.getIgnoreShortRecordsDuration()
        val duration = TimeUnit.MILLISECONDS
            .toSeconds(System.currentTimeMillis() - runningRecord.timeStarted)

        if (duration > durationToIgnore || durationToIgnore == 0L) {
            // No need to update widgets and notification because it will be done in running record remove.
            recordInteractor.addFromRunning(runningRecord)
        }
        activityStartedStoppedBroadcastInteractor.onActivityStopped(
            typeId = runningRecord.id,
            tagIds = runningRecord.tagIds,
            comment = runningRecord.comment,
        )
        remove(
            typeId = runningRecord.id,
            updateWidgets = updateWidgets,
            updateNotificationSwitch = updateNotificationSwitch,
        )
        pomodoroStopInteractor.checkAndStop(runningRecord.id)
    }

    suspend fun remove(
        typeId: Long,
        updateWidgets: Boolean = true,
        updateNotificationSwitch: Boolean = true,
    ) {
        runningRecordInteractor.remove(typeId)
        updateExternalViewsInteractor.onRunningRecordRemove(
            typeId = typeId,
            updateWidgets = updateWidgets,
            updateNotificationSwitch = updateNotificationSwitch
        )
    }
}