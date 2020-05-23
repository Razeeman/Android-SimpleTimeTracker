package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import javax.inject.Inject

class BackupInteractor @Inject constructor(
    private val backupRepo: BackupRepo
) {

    suspend fun saveBackupFile(uriString: String): BackupRepo.ResultCode {
        return backupRepo.saveBackupFile(uriString)
    }

    suspend fun restoreBackupFile(uriString: String): BackupRepo.ResultCode {
        return backupRepo.restoreBackupFile(uriString)
    }
}