package com.example.util.simpletimetracker.data_local.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.repo.DataEditRepo
import javax.inject.Inject

class DataEditRepoImpl @Inject constructor() : DataEditRepo {

    override val inProgress: LiveData<Boolean> = MutableLiveData(false)
}