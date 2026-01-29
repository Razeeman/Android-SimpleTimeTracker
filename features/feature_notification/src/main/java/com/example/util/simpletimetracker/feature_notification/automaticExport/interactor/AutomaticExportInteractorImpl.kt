package com.example.util.simpletimetracker.feature_notification.automaticExport.interactor

import com.example.util.simpletimetracker.core.extension.post
import com.example.util.simpletimetracker.core.repo.AutomaticExportRepo
import com.example.util.simpletimetracker.domain.backup.interactor.AutomaticExportInteractor
import com.example.util.simpletimetracker.domain.backup.interactor.CsvExportInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.backup.model.ResultCode
import com.example.util.simpletimetracker.feature_notification.automaticExport.scheduler.AutomaticExportScheduler
import com.example.util.simpletimetracker.feature_notification.core.GetTimeLeftToTimestampInteractor
import javax.inject.Inject

class AutomaticExportInteractorImpl @Inject constructor(
    private val scheduler: AutomaticExportScheduler,
    private val csvExportInteractor: CsvExportInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val automaticExportRepo: AutomaticExportRepo,
    private val getTimeLeftToTimestampInteractor: GetTimeLeftToTimestampInteractor,
) : AutomaticExportInteractor {

    override suspend fun schedule() {
        val triggerTime = prefsInteractor.getAutomaticExportTriggerTime()
        val timestamp = getTimeLeftToTimestampInteractor.execute(triggerTime)
        scheduler.schedule(timestamp)
    }

    override fun cancel() {
        scheduler.cancelSchedule()
    }

    override fun onFinished() {
        automaticExportRepo.inProgress.post(false)
    }

    override suspend fun export(): ResultCode? {
        automaticExportRepo.inProgress.post(true)

        val uri = prefsInteractor.getAutomaticExportUri()
            .takeUnless { it.isEmpty() }
            ?: run {
                onFinished()
                return null
            }
        val result = csvExportInteractor.saveCsvFile(
            uriString = uri,
            range = null,
            dateTimeFormat = prefsInteractor.getCsvExportDateTimeFormat(),
        )

        if (result is ResultCode.Success) {
            schedule()
            prefsInteractor.setAutomaticExportLastSaveTime(System.currentTimeMillis())
        } else {
            cancel()
            prefsInteractor.setAutomaticExportError(true)
            prefsInteractor.setAutomaticExportUri("")
        }

        onFinished()

        return result
    }
}