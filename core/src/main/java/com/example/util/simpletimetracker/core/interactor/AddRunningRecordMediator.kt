package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
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

    suspend fun tryStartTimer(
        typeId: Long,
        onNeedToShowTagSelection: () -> Unit
    ) {
        // Already running
        if (runningRecordInteractor.get(typeId) != null) return

        // Check if need to show tag selection
        if (prefsInteractor.getShowRecordTagSelection()) {
            val tags = recordTagInteractor.getByType(typeId)
                .filterNot { it.archived }

            if (tags.isEmpty()) {
                startTimer(typeId)
            } else {
                onNeedToShowTagSelection()
            }
        } else {
            startTimer(typeId)
        }
    }

    suspend fun startTimer(
        typeId: Long,
        tagId: Long = 0L
    ) {
        // Check if multitasking disabled
        if (!prefsInteractor.getAllowMultitasking()) {
            runningRecordInteractor.getAll()
                .filter { it.id != typeId }
                .forEach { removeRunningRecordMediator.removeWithRecordAdd(it) }
        }
        add(
            typeId = typeId,
            tagId = tagId
        )
    }

    suspend fun add(
        typeId: Long,
        timeStarted: Long? = null,
        comment: String = "",
        tagId: Long = 0L
    ) {
        if (runningRecordInteractor.get(typeId) == null) {
            RunningRecord(
                id = typeId,
                timeStarted = timeStarted ?: System.currentTimeMillis(),
                comment = comment,
                tagId = tagId
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