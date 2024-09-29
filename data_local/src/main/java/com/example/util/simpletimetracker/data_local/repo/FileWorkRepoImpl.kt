package com.example.util.simpletimetracker.data_local.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.repo.FileWorkRepo
import javax.inject.Inject

class FileWorkRepoImpl @Inject constructor() : FileWorkRepo {

    override val inProgress: LiveData<Boolean> = MutableLiveData(false)
}