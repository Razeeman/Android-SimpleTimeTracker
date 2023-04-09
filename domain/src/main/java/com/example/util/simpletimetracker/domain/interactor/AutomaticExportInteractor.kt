package com.example.util.simpletimetracker.domain.interactor

interface AutomaticExportInteractor {

    fun schedule()

    fun cancel()

    fun onFinished()

    suspend fun export()
}