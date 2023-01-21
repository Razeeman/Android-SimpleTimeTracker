package com.example.util.simpletimetracker.feature_notification.automaticExport.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.repo.AutomaticExportRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutomaticExportRepoImpl @Inject constructor() : AutomaticExportRepo {

    override val inProgress: LiveData<Boolean> = MutableLiveData(false)
}