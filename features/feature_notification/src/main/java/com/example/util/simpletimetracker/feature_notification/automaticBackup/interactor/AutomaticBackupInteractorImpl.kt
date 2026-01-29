package com.example.util.simpletimetracker.feature_notification.automaticBackup.interactor

import com.example.util.simpletimetracker.core.extension.post
import com.example.util.simpletimetracker.core.repo.AutomaticBackupRepo
import com.example.util.simpletimetracker.domain.backup.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.backup.interactor.BackupInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.backup.model.BackupOptionsData
import com.example.util.simpletimetracker.domain.backup.model.ResultCode
import com.example.util.simpletimetracker.feature_notification.automaticBackup.scheduler.AutomaticBackupScheduler
import com.example.util.simpletimetracker.feature_notification.core.GetTimeLeftToTimestampInteractor
import javax.inject.Inject

class AutomaticBackupInteractorImpl @Inject constructor(
    private val scheduler: AutomaticBackupScheduler,
    private val backupInteractor: BackupInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val automaticBackupRepo: AutomaticBackupRepo,
    private val getTimeLeftToTimestampInteractor: GetTimeLeftToTimestampInteractor,
) : AutomaticBackupInteractor {

    override suspend fun schedule() {
        val triggerTime = prefsInteractor.getAutomaticBackupTriggerTime()
        val timestamp = getTimeLeftToTimestampInteractor.execute(triggerTime)
        scheduler.schedule(timestamp)
    }

    override fun cancel() {
        scheduler.cancelSchedule()
    }

    override fun onFinished() {
        automaticBackupRepo.inProgress.post(false)
    }

    override suspend fun backup(): ResultCode? {
        automaticBackupRepo.inProgress.post(true)

        val uri = prefsInteractor.getAutomaticBackupUri()
            .takeUnless { it.isEmpty() }
            ?: run {
                onFinished()
                return null
            }
        val result = backupInteractor.saveBackupFile(uri, BackupOptionsData.Save.Standard)

        if (result is ResultCode.Success) {
            schedule()
            prefsInteractor.setAutomaticBackupLastSaveTime(System.currentTimeMillis())
        } else {
            cancel()
            prefsInteractor.setAutomaticBackupError(true)
            prefsInteractor.setAutomaticBackupUri("")
        }

        onFinished()

        return result
    }
}