package com.example.util.simpletimetracker.domain.interactor

interface AutomaticBackupInteractor {

    fun schedule()

    fun cancel()

    suspend fun backup()
}