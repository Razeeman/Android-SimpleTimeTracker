package com.example.util.simpletimetracker.domain.interactor

interface NotificationInactivityInteractor {

    suspend fun checkAndSchedule()

    fun cancel()

    suspend fun show()
}