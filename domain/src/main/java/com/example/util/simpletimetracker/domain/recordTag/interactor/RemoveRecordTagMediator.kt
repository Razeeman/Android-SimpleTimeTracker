package com.example.util.simpletimetracker.domain.recordTag.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import javax.inject.Inject

class RemoveRecordTagMediator @Inject constructor(
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun remove(
        tagId: Long,
        fromArchive: Boolean,
    ) {
        val removedGoalIds = recordTypeGoalInteractor.getByTag(tagId).map { it.id }
        recordTagInteractor.remove(tagId)
        doAfterRemove(tagId, fromArchive, removedGoalIds)
    }

    private suspend fun doAfterRemove(
        tagId: Long,
        fromArchive: Boolean,
        removedGoalIds: List<Long>,
    ) {
        externalViewsInteractor.onTagRemove(
            tagId = tagId,
            fromArchive = fromArchive,
            removedGoalIds = removedGoalIds,
        )
    }
}