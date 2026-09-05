package com.example.util.simpletimetracker.domain.recordType.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import javax.inject.Inject

class RemoveRecordTypeMediator @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun remove(
        typeId: Long,
        fromArchive: Boolean,
    ) {
        recordTypeInteractor.remove(typeId)
        doAfterRemove(typeId, fromArchive)
    }

    private suspend fun doAfterRemove(
        typeId: Long,
        fromArchive: Boolean,
    ) {
        externalViewsInteractor.onTypeRemove(typeId, fromArchive)
    }
}