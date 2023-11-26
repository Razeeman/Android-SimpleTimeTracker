package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import javax.inject.Inject

class BackupInteractor @Inject constructor(
    private val backupRepo: BackupRepo,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
) {

    suspend fun saveBackupFile(uriString: String): ResultCode {
        return backupRepo.saveBackupFile(uriString)
    }

    suspend fun restoreBackupFile(uriString: String): ResultCode {
        val resultCode = backupRepo.restoreBackupFile(uriString)

        val runningRecords = runningRecordInteractor.getAll()
        notificationGoalTimeInteractor.checkAndReschedule(runningRecords.map { it.id })
        widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
        widgetInteractor.updateWidgets(listOf(WidgetType.RECORD_TYPE))

        return resultCode
    }
}