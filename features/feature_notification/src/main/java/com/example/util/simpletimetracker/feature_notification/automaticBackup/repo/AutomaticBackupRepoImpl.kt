package com.example.util.simpletimetracker.feature_notification.automaticBackup.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.repo.AutomaticBackupRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutomaticBackupRepoImpl @Inject constructor() : AutomaticBackupRepo {

    override val inProgress: LiveData<Boolean> = MutableLiveData(false)
}