package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import javax.inject.Inject

class RemoveRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun remove(
        recordIds: List<Long>,
        typeIds: List<Long>,
        tagIds: List<Long>,
    ) {
        recordIds.forEach { recordInteractor.remove(it) }
        doAfterRemove(typeIds = typeIds, tagIds = tagIds)
    }

    suspend fun doAfterRemove(
        typeIds: List<Long>,
        tagIds: List<Long>,
    ) {
        externalViewsInteractor.onRecordRemove(typeIds = typeIds, tagIds = tagIds)
    }
}