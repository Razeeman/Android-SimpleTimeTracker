package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import javax.inject.Inject

class AddRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun add(
        record: Record,
        updateNotificationSwitch: Boolean = true,
    ) {
        add(
            records = listOf(record),
            updateNotificationSwitch = updateNotificationSwitch,
        )
    }

    suspend fun add(
        records: List<Record>,
        updateNotificationSwitch: Boolean = true,
    ) {
        records.forEach { recordInteractor.add(it) }
        doAfterAdd(
            typeIds = records.map(Record::typeId).distinct(),
            tagIds = records.map(Record::tags).flatten().map(RecordBase.Tag::tagId).distinct(),
            updateNotificationSwitch = updateNotificationSwitch,
        )
    }

    suspend fun doAfterAdd(
        typeIds: List<Long>,
        tagIds: List<Long>,
        updateNotificationSwitch: Boolean = true,
    ) {
        externalViewsInteractor.onRecordAddOrChange(
            typeIds = typeIds,
            tagIds = tagIds,
            updateNotificationSwitch = updateNotificationSwitch,
        )
    }
}