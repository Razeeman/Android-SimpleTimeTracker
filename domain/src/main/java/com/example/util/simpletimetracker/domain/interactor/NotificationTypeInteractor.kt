package com.example.util.simpletimetracker.domain.interactor

interface NotificationTypeInteractor {

    suspend fun checkAndShow(typeId: Long)

    suspend fun checkAndHide(typeId: Long)

    suspend fun updateNotifications()
}