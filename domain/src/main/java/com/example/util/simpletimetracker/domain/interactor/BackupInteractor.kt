package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import javax.inject.Inject

class BackupInteractor @Inject constructor(
    private val backupRepo: BackupRepo
) {

    suspend fun saveBackupFile(uriString: String): ResultCode {
        return backupRepo.saveBackupFile(uriString)
    }

    suspend fun restoreBackupFile(uriString: String): ResultCode {
        return backupRepo.restoreBackupFile(uriString)
    }
}