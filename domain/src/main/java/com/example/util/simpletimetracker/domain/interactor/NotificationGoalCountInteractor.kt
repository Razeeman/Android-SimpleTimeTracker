package com.example.util.simpletimetracker.domain.interactor

interface NotificationGoalCountInteractor {

    suspend fun checkAndShow(typeId: Long)

    fun cancel(typeId: Long)
}