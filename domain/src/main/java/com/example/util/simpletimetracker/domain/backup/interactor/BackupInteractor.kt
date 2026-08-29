package com.example.util.simpletimetracker.domain.backup.interactor

import com.example.util.simpletimetracker.domain.backup.model.BackupOptionsData
import com.example.util.simpletimetracker.domain.backup.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.domain.backup.model.ResultCode
import com.example.util.simpletimetracker.domain.backup.repo.BackupPartialRepo
import com.example.util.simpletimetracker.domain.backup.repo.BackupRepo
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import javax.inject.Inject

class BackupInteractor @Inject constructor(
    private val backupRepo: BackupRepo,
    private val backupPartialRepo: BackupPartialRepo,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun saveBackupFile(
        uriString: String,
        params: BackupOptionsData.Save,
    ): ResultCode {
        return backupRepo.saveBackupFile(uriString, params)
    }

    suspend fun restoreBackupFile(
        uriString: String,
        params: BackupOptionsData.Restore,
    ): ResultCode {
        val resultCode = backupRepo.restoreBackupFile(uriString, params)
        if (resultCode is ResultCode.Success) doAfterRestore()
        return resultCode
    }

    suspend fun partialRestoreBackupFile(
        params: BackupOptionsData.Custom,
    ): ResultCode {
        val resultCode = backupPartialRepo.partialRestoreBackupFile(params)
        if (resultCode is ResultCode.Success) doAfterRestore()
        return resultCode
    }

    suspend fun readBackupFileContent(
        uriString: String,
    ): Pair<ResultCode, PartialBackupRestoreData?> {
        return backupPartialRepo.readBackupFile(uriString)
    }

    suspend fun doAfterRestore() {
        externalViewsInteractor.onBackupRestore()
    }
}