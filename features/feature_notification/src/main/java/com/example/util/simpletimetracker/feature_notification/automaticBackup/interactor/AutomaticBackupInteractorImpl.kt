package com.example.util.simpletimetracker.feature_notification.automaticBackup.interactor

import com.example.util.simpletimetracker.core.extension.post
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.AutomaticBackupRepo
import com.example.util.simpletimetracker.domain.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.interactor.BackupInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import com.example.util.simpletimetracker.feature_notification.automaticBackup.scheduler.AutomaticBackupScheduler
import javax.inject.Inject

class AutomaticBackupInteractorImpl @Inject constructor(
    private val scheduler: AutomaticBackupScheduler,
    private val backupInteractor: BackupInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val automaticBackupRepo: AutomaticBackupRepo,
    private val timeMapper: TimeMapper,
) : AutomaticBackupInteractor {

    override fun schedule() {
        val current = System.currentTimeMillis()
        val timestamp = timeMapper
            .getRangeStartAndEnd(
                rangeLength = RangeLength.Day,
                shift = 0,
                firstDayOfWeek = DayOfWeek.MONDAY, // not needed.
                startOfDayShift = 0, // not needed.
            )
            .second
            .let { it - current }
            .takeIf { it > 0 }
            ?: current

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