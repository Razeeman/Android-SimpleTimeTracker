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
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor
) {

    /**
     * Returns true if activity was started.
     */
    suspend fun tryStartTimer(
        typeId: Long,
        onNeedToShowTagSelection: () -> Unit
    ): Boolean {
        // Already running
        if (runningRecordInteractor.get(typeId) != null) return false

        // Check if need to show tag selection
        return if (prefsInteractor.getShowRecordTagSelection()) {
            val tags = recordTagInteractor.getByTypeOrUntyped(typeId)
                .filterNot { it.archived }

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
        add(
            typeId = typeId,
            comment = comment,
            tagIds = tagIds,
            timeStarted = timeStarted,
        )
    }

    suspend fun add(
        typeId: Long,
        timeStarted: Long? = null,
        comment: String = "",
        tagIds: List<Long> = emptyList()
    ) {
        if (runningRecordInteractor.get(typeId) == null) {
            RunningRecord(
                id = typeId,
                timeStarted = timeStarted ?: System.currentTimeMillis(),
                comment = comment,
                tagIds = tagIds
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