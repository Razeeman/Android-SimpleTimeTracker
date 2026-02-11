package com.example.util.simpletimetracker.domain.notifications.interactor

import com.example.util.simpletimetracker.domain.record.model.RecordBase

interface NotificationTypeInteractor {

    suspend fun checkAndShow(
        typeId: Long,
        typesShift: Int = 0,
        tagsShift: Int = 0,
        selectedTypeId: Long = 0,
        selectedTags: List<RecordBase.Tag> = emptyList(),
        editingTagId: Long? = null,
        editingTagValueInput: String? = null,
        isMultipleTagAvailable: Boolean = false,
    )

    suspend fun checkAndHide(typeId: Long)

    suspend fun updateNotifications()
}