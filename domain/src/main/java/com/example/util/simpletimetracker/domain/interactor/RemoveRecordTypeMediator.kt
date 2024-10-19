package com.example.util.simpletimetracker.domain.interactor

import javax.inject.Inject

class RemoveRecordTypeMediator @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun remove(typeId: Long) {
        recordTypeInteractor.remove(typeId)
        doAfterRemove(typeId)
    }

    private suspend fun doAfterRemove(typeId: Long) {
        externalViewsInteractor.onTypeRemove(typeId)
    }
}