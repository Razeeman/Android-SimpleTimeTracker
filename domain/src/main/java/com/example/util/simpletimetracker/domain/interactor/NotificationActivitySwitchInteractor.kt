package com.example.util.simpletimetracker.domain.interactor

interface NotificationActivitySwitchInteractor {

    suspend fun updateNotification(
        typesShift: Int = 0,
        tagsShift: Int = 0,
        selectedTypeId: Long = 0,
    )
}