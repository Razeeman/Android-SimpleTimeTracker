package com.example.util.simpletimetracker.domain.interactor

interface NotificationGoalRangeEndInteractor {

    suspend fun checkAndReschedule()

    fun cancel()
}