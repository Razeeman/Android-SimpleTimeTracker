package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.ActivityStartedStoppedBroadcastInteractor
import com.example.util.simpletimetracker.domain.pomodoro.interactor.PomodoroStopInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
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
        timeEnded: Long? = null, // null - take current time.
    ) {
        val recordTimeEnded = timeEnded
            ?.coerceAtLeast(runningRecord.timeStarted)
            ?: System.currentTimeMillis()
        val durationToIgnore = prefsInteractor.getIgnoreShortRecordsDuration()
        val duration = TimeUnit.MILLISECONDS
            .toSeconds(recordTimeEnded - runningRecord.timeStarted)

        if (duration > durationToIgnore || durationToIgnore == 0L) {
            // No need to update widgets and notification because it will be done in running record remove.
            recordInteractor.addFromRunning(
                runningRecord = runningRecord,
                timeEnded = recordTimeEnded,
            )
        }
        activityStartedStoppedBroadcastInteractor.onActivityStopped(
            typeId = runningRecord.id,
            tagIds = runningRecord.tags.map(RecordBase.Tag::tagId),
            comment = runningRecord.comment,
        )
        remove(
            typeId = runningRecord.id,
            updateWidgets = updateWidgets,
            updateNotificationSwitch = updateNotificationSwitch,
        )
    }

    suspend fun remove(
        typeId: Long,
        updateWidgets: Boolean = true,
        updateNotificationSwitch: Boolean = true,
        checkPomodoroStop: Boolean = true,
    ) {
        val runningRecord = runningRecordInteractor.get(typeId)
        runningRecordInteractor.remove(typeId)
        updateExternalViewsInteractor.onRunningRecordRemove(
            typeId = typeId,
            tagIds = runningRecord?.tags.orEmpty().map(RecordBase.Tag::tagId),
            updateWidgets = updateWidgets,
            updateNotificationSwitch = updateNotificationSwitch,
        )
        if (checkPomodoroStop) pomodoroStopInteractor.checkAndStop(typeId)
    }
}