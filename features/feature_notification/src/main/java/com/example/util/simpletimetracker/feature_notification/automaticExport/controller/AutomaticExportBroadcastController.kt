package com.example.util.simpletimetracker.feature_notification.automaticExport.controller

import com.example.util.simpletimetracker.domain.interactor.AutomaticExportInteractor
import javax.inject.Inject

class AutomaticExportBroadcastController @Inject constructor(
    private val automaticExportInteractor: AutomaticExportInteractor,
) {

    suspend fun onReminder() {
        automaticExportInteractor.export()
    }

    fun onBootCompleted() {
        automaticExportInteractor.schedule()
    }
}