package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.BackupOptionsData
import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import javax.inject.Inject

class BackupInteractor @Inject constructor(
    private val backupRepo: BackupRepo,
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
        doAfterRestore()
        return resultCode
    }

    suspend fun partialRestoreBackupFile(
        params: BackupOptionsData.Custom,
    ): ResultCode {
        val resultCode = backupRepo.partialRestoreBackupFile(params)
        doAfterRestore()
        return resultCode
    }

    suspend fun readBackupFileContent(
        uriString: String,
    ): Pair<ResultCode, PartialBackupRestoreData?> {
        return backupRepo.readBackupFile(uriString)
    }

    suspend fun doAfterRestore() {
        externalViewsInteractor.onBackupRestore()
    }
}