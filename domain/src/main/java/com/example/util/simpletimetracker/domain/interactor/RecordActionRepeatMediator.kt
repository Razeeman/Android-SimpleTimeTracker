package com.example.util.simpletimetracker.domain.interactor

import javax.inject.Inject

class RecordActionRepeatMediator @Inject constructor(
    private val runningRecordInteractor: RunningRecordInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
) {

    suspend fun execute(
        typeId: Long,
        comment: String,
        tagIds: List<Long>,
    ) {
        // Stop same type running record if exist (only one of the same type can run at once).
        // Widgets will update on adding.
        runningRecordInteractor.get(typeId)
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it, updateWidgets = false) }
        // Add new running record.
        addRunningRecordMediator.startTimer(
            typeId = typeId,
            comment = comment,
            tagIds = tagIds,
        )
    }
}