package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.extension.orZero
import javax.inject.Inject

class RecordActionContinueMediator @Inject constructor(
    private val runningRecordInteractor: RunningRecordInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordInteractor: RecordInteractor,
    private val removeRecordMediator: RemoveRecordMediator,
) {

    suspend fun execute(
        recordId: Long?,
        typeId: Long,
        timeStarted: Long,
        comment: String,
        tagIds: List<Long>,
    ) {
        // Remove current record if exist.
        recordId?.let {
            val oldTypeId = recordInteractor.get(it)?.typeId.orZero()
            removeRecordMediator.remove(it, oldTypeId)
        }
        // Stop same type running record if exist (only one of the same type can run at once).
        // Widgets will update on adding.
        runningRecordInteractor.get(typeId)
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it, updateWidgets = false) }
        // Add new running record.
        addRunningRecordMediator.startTimer(
            typeId = typeId,
            timeStarted = timeStarted,
            comment = comment,
            tagIds = tagIds,
        )
    }
}