package com.example.util.simpletimetracker.domain.recordType.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import javax.inject.Inject

class RemoveRecordTypeMediator @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun remove(
        typeId: Long,
        fromArchive: Boolean,
    ) {
        val removedGoalIds = recordTypeGoalInteractor.getByType(typeId).map { it.id }
        recordTypeInteractor.remove(typeId)
        doAfterRemove(typeId, fromArchive, removedGoalIds)
    }

    private suspend fun doAfterRemove(
        typeId: Long,
        fromArchive: Boolean,
        removedGoalIds: List<Long>,
    ) {
        externalViewsInteractor.onTypeRemove(
            typeId = typeId,
            fromArchive = fromArchive,
            removedGoalIds = removedGoalIds,
        )
    }
}