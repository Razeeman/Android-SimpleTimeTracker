package com.example.util.simpletimetracker.domain.notifications.interactor

import com.example.util.simpletimetracker.domain.record.model.RecordBase

interface NotificationActivitySwitchInteractor {

    suspend fun updateNotification(
        typesShift: Int = 0,
        tagsShift: Int = 0,
        selectedTypeId: Long = 0,
        selectedTags: List<RecordBase.Tag> = emptyList(),
        editingTagId: Long? = null,
        editingTagValueInput: String? = null,
        isMultipleTagAvailable: Boolean = false,
    )
}