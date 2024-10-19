package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject

class AddRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun add(
        record: Record,
        updateNotificationSwitch: Boolean = true,
    ) {
        recordInteractor.add(record)
        doAfterAdd(
            typeId = record.typeId,
            updateNotificationSwitch = updateNotificationSwitch,
        )
    }

    suspend fun doAfterAdd(
        typeId: Long,
        updateNotificationSwitch: Boolean = true,
    ) {
        externalViewsInteractor.onRecordAddOrChange(
            typeId = typeId,
            updateNotificationSwitch = updateNotificationSwitch,
        )
    }
}