package com.example.util.simpletimetracker.feature_notification.automaticBackup.interactor

import com.example.util.simpletimetracker.core.extension.post
import com.example.util.simpletimetracker.core.repo.AutomaticBackupRepo
import com.example.util.simpletimetracker.domain.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.interactor.BackupInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import com.example.util.simpletimetracker.feature_notification.automaticBackup.scheduler.AutomaticBackupScheduler
import com.example.util.simpletimetracker.feature_notification.core.GetTimeToDayEndInteractor
import javax.inject.Inject

class AutomaticBackupInteractorImpl @Inject constructor(
    private val scheduler: AutomaticBackupScheduler,
    private val backupInteractor: BackupInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val automaticBackupRepo: AutomaticBackupRepo,
    private val getTimeToDayEndInteractor: GetTimeToDayEndInteractor,
) : AutomaticBackupInteractor {

    override fun schedule() {
        val timestamp = getTimeToDayEndInteractor.execute()
        scheduler.schedule(timestamp)
    }

    override fun cancel() {
        scheduler.cancelSchedule()
    }

    override suspend fun backup() {
        schedule()
        automaticBackupRepo.inProgress.post(true)
        val uri = prefsInteractor.getAutomaticBackupUri()
        val result = backupInteractor.saveBackupFile(uri)

        if (result == ResultCode.SUCCESS) {
            schedule()
            prefsInteractor.setAutomaticBackupLastSaveTime(System.currentTimeMillis())
        } else {
            cancel()
            prefsInteractor.setAutomaticBackupError(true)
            prefsInteractor.setAutomaticBackupUri("")
        }
        automaticBackupRepo.inProgress.post(false)
    }
}