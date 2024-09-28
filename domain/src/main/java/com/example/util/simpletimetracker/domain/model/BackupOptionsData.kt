package com.example.util.simpletimetracker.domain.model

sealed interface BackupOptionsData {

    sealed interface Save : BackupOptionsData {
        object Standard : Save
        object SaveWithoutRecords : Save
    }

    sealed interface Restore : BackupOptionsData {
        object Standard : Restore
        object WithSettings : Restore
    }

    data class Custom(val data: PartialBackupRestoreData) : BackupOptionsData
}