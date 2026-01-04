package com.example.util.simpletimetracker.domain.recordTag.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
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
        doAfterRemove(tagId, fromArchive)
    }

    private suspend fun doAfterRemove(
        tagId: Long,
        fromArchive: Boolean,
    ) {
        externalViewsInteractor.onTagRemove(tagId, fromArchive)
    }
}