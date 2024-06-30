package com.example.util.simpletimetracker.domain.interactor

interface PomodoroCycleNotificationInteractor {

    suspend fun checkAndReschedule()

    fun cancel()
}