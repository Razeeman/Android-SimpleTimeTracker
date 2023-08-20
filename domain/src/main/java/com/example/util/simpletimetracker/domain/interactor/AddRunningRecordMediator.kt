package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class AddRunningRecordMediator @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val notificationGoalCountInteractor: NotificationGoalCountInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val activityStartedStoppedBroadcastInteractor: ActivityStartedStoppedBroadcastInteractor,
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

        // Check if need to show tag selection
        return if (prefsInteractor.getShowRecordTagSelection()) {
            val tags = if (prefsInteractor.getRecordTagSelectionEvenForGeneralTags()) {
                recordTagInteractor.getByTypeOrUntyped(typeId)
            } else {
                recordTagInteractor.getByType(typeId)
            }.filterNot { it.archived }

            // TODO add query to repo to find out if has tags.
            if (tags.isEmpty()) {
                startTimer(typeId, emptyList(), "")
                true
            } else {
                onNeedToShowTagSelection()
                false
            }
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
    ) {
        // Check if multitasking disabled
        if (!prefsInteractor.getAllowMultitasking()) {
            runningRecordInteractor.getAll()
                .filter { it.id != typeId }
                .forEach { removeRunningRecordMediator.removeWithRecordAdd(it) }
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
    }

    suspend fun add(
        typeId: Long,
        timeStarted: Long? = null,
        comment: String = "",
        tagIds: List<Long> = emptyList(),
    ) {
        if (runningRecordInteractor.get(typeId) == null && typeId > 0L) {
            RunningRecord(
                id = typeId,
                timeStarted = timeStarted ?: System.currentTimeMillis(),
                comment = comment,
                tagIds = tagIds,
            ).let {
                runningRecordInteractor.add(it)
                notificationTypeInteractor.checkAndShow(typeId)
                notificationInactivityInteractor.cancel()
                // Schedule only on first activity start.
                if (runningRecordInteractor.getAll().size == 1) notificationActivityInteractor.checkAndSchedule()
                notificationGoalTimeInteractor.checkAndReschedule(typeId)
                widgetInteractor.updateWidgets()
            }
        }
    }
}