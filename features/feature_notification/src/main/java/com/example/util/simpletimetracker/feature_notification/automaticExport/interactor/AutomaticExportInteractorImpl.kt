package com.example.util.simpletimetracker.feature_notification.automaticExport.interactor

import com.example.util.simpletimetracker.core.extension.post
import com.example.util.simpletimetracker.core.repo.AutomaticExportRepo
import com.example.util.simpletimetracker.domain.interactor.AutomaticExportInteractor
import com.example.util.simpletimetracker.domain.interactor.CsvExportInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import com.example.util.simpletimetracker.feature_notification.automaticExport.scheduler.AutomaticExportScheduler
import com.example.util.simpletimetracker.feature_notification.core.GetTimeToDayEndInteractor
import javax.inject.Inject

class AutomaticExportInteractorImpl @Inject constructor(
    private val scheduler: AutomaticExportScheduler,
    private val csvExportInteractor: CsvExportInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val automaticExportRepo: AutomaticExportRepo,
    private val getTimeToDayEndInteractor: GetTimeToDayEndInteractor,
) : AutomaticExportInteractor {

    override fun schedule() {
        val timestamp = getTimeToDayEndInteractor.execute()
        scheduler.schedule(timestamp)
    }

    override fun cancel() {
        scheduler.cancelSchedule()
    }

    override fun onFinished() {
        automaticExportRepo.inProgress.post(false)
    }

    override suspend fun export() {
        automaticExportRepo.inProgress.post(true)

        val uri = prefsInteractor.getAutomaticExportUri()
            .takeUnless { it.isEmpty() }
            ?: run {
                onFinished()
                return
            }
        val result = csvExportInteractor.saveCsvFile(uri, range = null)

        if (result == ResultCode.SUCCESS) {
            schedule()
            prefsInteractor.setAutomaticExportLastSaveTime(System.currentTimeMillis())
        } else {
            cancel()
            prefsInteractor.setAutomaticExportError(true)
            prefsInteractor.setAutomaticExportUri("")
        }

        onFinished()
    }
}