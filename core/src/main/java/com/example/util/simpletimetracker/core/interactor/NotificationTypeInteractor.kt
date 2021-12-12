package com.example.util.simpletimetracker.core.interactor

interface NotificationTypeInteractor {

    suspend fun checkAndShow(typeId: Long)

    suspend fun checkAndHide(typeId: Long)

    suspend fun updateNotifications()
}