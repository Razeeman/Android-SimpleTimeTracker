package com.example.util.simpletimetracker.feature_notification.automaticExport.controller

import com.example.util.simpletimetracker.domain.backup.interactor.AutomaticExportInteractor
import javax.inject.Inject

class AutomaticExportBroadcastController @Inject constructor(
    private val automaticExportInteractor: AutomaticExportInteractor,
) {

    suspend fun onReminder() {
        automaticExportInteractor.export()
    }

    fun onFinished() {
        automaticExportInteractor.onFinished()
    }

    suspend fun onBootCompleted() {
        automaticExportInteractor.schedule()
    }
}