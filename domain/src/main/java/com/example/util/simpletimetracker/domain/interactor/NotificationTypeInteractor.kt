package com.example.util.simpletimetracker.domain.interactor

interface NotificationTypeInteractor {

    suspend fun checkAndShow(typeId: Long, typesShift: Int = 0)

    suspend fun checkAndHide(typeId: Long)

    suspend fun updateNotifications()
}