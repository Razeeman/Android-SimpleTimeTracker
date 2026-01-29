package com.example.util.simpletimetracker.domain.backup.interactor

import com.example.util.simpletimetracker.domain.backup.model.ResultCode

interface AutomaticBackupInteractor {

    suspend fun schedule()

    fun cancel()

    fun onFinished()

    suspend fun backup(): ResultCode?
}