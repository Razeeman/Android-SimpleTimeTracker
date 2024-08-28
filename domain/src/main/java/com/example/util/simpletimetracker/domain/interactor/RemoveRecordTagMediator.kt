package com.example.util.simpletimetracker.domain.interactor

import javax.inject.Inject

class RemoveRecordTagMediator @Inject constructor(
    private val recordTagInteractor: RecordTagInteractor,
    private val wearInteractor: WearInteractor,
) {

    suspend fun remove(tagId: Long) {
        recordTagInteractor.remove(tagId)
        doAfterRemove()
    }

    private suspend fun doAfterRemove() {
        wearInteractor.update()
    }
}