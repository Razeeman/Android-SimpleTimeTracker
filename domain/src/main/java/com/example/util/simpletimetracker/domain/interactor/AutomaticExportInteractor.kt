package com.example.util.simpletimetracker.domain.interactor

interface AutomaticExportInteractor {

    fun schedule()

    fun cancel()

    suspend fun export()
}