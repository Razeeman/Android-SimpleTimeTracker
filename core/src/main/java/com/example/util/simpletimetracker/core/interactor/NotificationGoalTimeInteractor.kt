package com.example.util.simpletimetracker.core.interactor

interface NotificationGoalTimeInteractor {

    suspend fun checkAndReschedule(typeId: Long)

    fun cancel(typeId: Long)

    fun show(typeId: Long)
}