package com.example.util.simpletimetracker.domain.interactor

import javax.inject.Inject

class RemoveRecordTagMediator @Inject constructor(
    private val recordTagInteractor: RecordTagInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun remove(
        tagId: Long,
        fromArchive: Boolean,
    ) {
        recordTagInteractor.remove(tagId)
        doAfterRemove(fromArchive)
    }

    private suspend fun doAfterRemove(
        fromArchive: Boolean,
    ) {
        externalViewsInteractor.onTagRemove(fromArchive)
    }
}