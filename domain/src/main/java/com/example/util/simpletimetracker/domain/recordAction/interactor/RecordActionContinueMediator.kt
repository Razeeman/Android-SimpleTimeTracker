package com.example.util.simpletimetracker.domain.recordAction.interactor

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
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
        tags: List<RecordBase.Tag>,
    ) {
        // Remove current record if exist.
        recordId?.let {
            val oldRecord = recordInteractor.get(it)
            removeRecordMediator.remove(
                recordIds = listOf(it),
                typeIds = listOf(oldRecord?.typeId.orZero()),
                tagIds = oldRecord?.tags.orEmpty().map(RecordBase.Tag::tagId),
            )
        }
        // Stop same type running record if exist (only one of the same type can run at once).
        // Widgets will update on adding.
        runningRecordInteractor.get(typeId)
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it, updateWidgets = false) }
        // Add new running record.
        addRunningRecordMediator.startTimer(
            typeId = typeId,
            comment = comment,
            tags = tags,
            timeStarted = AddRunningRecordMediator.StartTime.Timestamp(timeStarted),
            checkDefaultDuration = false,
        )
    }
}