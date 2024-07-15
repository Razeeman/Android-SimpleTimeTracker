package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject

class RecordActionDuplicateMediator @Inject constructor(
    private val addRecordMediator: AddRecordMediator,
) {

    suspend fun execute(
        typeId: Long,
        timeStarted: Long,
        timeEnded: Long,
        comment: String,
        tagIds: List<Long>,
    ) {
        Record(
            typeId = typeId,
            timeStarted = timeStarted,
            timeEnded = timeEnded,
            comment = comment,
            tagIds = tagIds,
        ).let {
            addRecordMediator.add(it)
        }
    }
}