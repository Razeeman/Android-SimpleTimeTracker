package com.example.util.simpletimetracker.domain.interactor

interface NotificationActivityInteractor {

    suspend fun checkAndSchedule()

    fun cancel()

    suspend fun show()
}