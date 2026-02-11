package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.base.suspendLazy
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToDefaultTagInteractor
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LoadPreselectedTagsInteractor @Inject constructor(
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val recordTypeToDefaultTagInteractor: RecordTypeToDefaultTagInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    suspend fun execute(typeId: Long) = coroutineScope {
        val defaultTags = recordTypeToDefaultTagInteractor.getTags(typeId)
        val timeStarted = currentTimestampProvider.get()
        val ruleTags = addRunningRecordMediator.processRules(
            typeId = typeId,
            timeStarted = timeStarted,
            prevRecords = suspendLazy { emptyList() },
        ).tagsIds
        defaultTags + ruleTags
    }
}