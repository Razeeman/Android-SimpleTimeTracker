package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class AddRunningRecordMediator @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTypeToDefaultTagInteractor: RecordTypeToDefaultTagInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val notificationGoalCountInteractor: NotificationGoalCountInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val wearInteractor: WearInteractor,
    private val activityStartedStoppedBroadcastInteractor: ActivityStartedStoppedBroadcastInteractor,
    private val shouldShowTagSelectionInteractor: ShouldShowTagSelectionInteractor,
    private val pomodoroStartInteractor: PomodoroStartInteractor,
) {

    /**
     * Returns true if activity was started.
     */
    suspend fun tryStartTimer(
        typeId: Long,
        onNeedToShowTagSelection: suspend () -> Unit,
    ): Boolean {
        // Already running
        if (runningRecordInteractor.get(typeId) != null) return false

        return if (shouldShowTagSelectionInteractor.execute(typeId)) {
            onNeedToShowTagSelection()
            false
        } else {
            startTimer(typeId, emptyList(), "")
            true
        }
    }

    suspend fun startTimer(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
        timeStarted: Long? = null,
        checkMultitasking: Boolean = true,
    ) {
        // Check if multitasking disabled
        if (!prefsInteractor.getAllowMultitasking() && checkMultitasking) {
            // Widgets will update on adding.
            runningRecordInteractor.getAll()
                .filter { it.id != typeId }
                .forEach { removeRunningRecordMediator.removeWithRecordAdd(it, updateWidgets = false) }
        }
        activityStartedStoppedBroadcastInteractor.onActionActivityStarted(
            typeId = typeId,
            tagIds = tagIds,
            comment = comment,
        )
        add(
            typeId = typeId,
            comment = comment,
            tagIds = tagIds,
            timeStarted = timeStarted,
        )
        // Show goal count only on timer start, otherwise it would show on change also.
        notificationGoalCountInteractor.checkAndShow(typeId)
        pomodoroStartInteractor.checkAndStart(typeId)
    }

    suspend fun add(
        typeId: Long,
        timeStarted: Long? = null,
        comment: String = "",
        tagIds: List<Long> = emptyList(),
    ) {
        if (runningRecordInteractor.get(typeId) == null && typeId > 0L) {
            val defaultTags = recordTypeToDefaultTagInteractor.getTags(typeId)
            RunningRecord(
                id = typeId,
                timeStarted = timeStarted ?: System.currentTimeMillis(),
                comment = comment,
                tagIds = (tagIds + defaultTags).toSet().toList(),
            ).let {
                runningRecordInteractor.add(it)
                notificationTypeInteractor.checkAndShow(typeId)
                notificationInactivityInteractor.cancel()
                // Schedule only on first activity start.
                if (runningRecordInteractor.getAll().size == 1) notificationActivityInteractor.checkAndSchedule()
                notificationGoalTimeInteractor.checkAndReschedule(listOf(typeId))
                widgetInteractor.updateWidgets()
                wearInteractor.update()
            }
        }
    }
}