package com.example.util.simpletimetracker.core.interactor

interface NotificationInactivityInteractor {

    suspend fun checkAndSchedule()

    fun cancel()

    fun show()
}