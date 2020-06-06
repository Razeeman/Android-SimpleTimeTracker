package com.example.util.simpletimetracker.domain.resolver

interface BackupRepo {

    enum class ResultCode {
        SUCCESS, ERROR
    }

    suspend fun saveBackupFile(uriString: String): ResultCode

    suspend fun restoreBackupFile(uriString: String): ResultCode
}