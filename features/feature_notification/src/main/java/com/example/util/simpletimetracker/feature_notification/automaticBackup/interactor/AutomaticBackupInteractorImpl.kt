package com.example.util.simpletimetracker.feature_notification.automaticBackup.interactor

import com.example.util.simpletimetracker.domain.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.interactor.BackupInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import com.example.util.simpletimetracker.feature_notification.automaticBackup.scheduler.AutomaticBackupScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AutomaticBackupInteractorImpl @Inject constructor(
    private val scheduler: AutomaticBackupScheduler,
    private val backupInteractor: BackupInteractor,
    private val prefsInteractor: PrefsInteractor,
) : AutomaticBackupInteractor {

    override fun schedule() {
        // TODO schedule at midnight
        val timestamp = TimeUnit.MINUTES.toMillis(1)
        scheduler.schedule(timestamp)
    }

    override fun cancel() {
        scheduler.cancelSchedule()
    }

    override suspend fun backup() {
        val uri = prefsInteractor.getAutomaticBackupUri()
        val result = backupInteractor.saveBackupFile(uri)

        if (result == ResultCode.SUCCESS) {
            // TODO add progress loader if app is opened at this time
            schedule()
        } else {
            prefsInteractor.setAutomaticBackupError(true)
            prefsInteractor.setAutomaticBackupUri("")
        }
    }
}