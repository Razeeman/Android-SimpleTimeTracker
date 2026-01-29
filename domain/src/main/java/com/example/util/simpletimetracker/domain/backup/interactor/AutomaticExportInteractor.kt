package com.example.util.simpletimetracker.domain.backup.interactor

import com.example.util.simpletimetracker.domain.backup.model.ResultCode

interface AutomaticExportInteractor {

    suspend fun schedule()

    fun cancel()

    fun onFinished()

    suspend fun export(): ResultCode?
}