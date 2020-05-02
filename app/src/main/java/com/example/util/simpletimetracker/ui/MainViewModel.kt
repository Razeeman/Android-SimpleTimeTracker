package com.example.util.simpletimetracker.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.data.TimePeriodDBO
import com.example.util.simpletimetracker.data.TimePeriodDao
import javax.inject.Inject

class MainViewModel : ViewModel() {

    @Inject
    lateinit var timePeriodDao: TimePeriodDao

    private val periodsLiveData: LiveData<List<TimePeriodDBO>> by lazy {
        return@lazy timePeriodDao.getAll()
    }

    val periods: LiveData<List<TimePeriodDBO>> get() = periodsLiveData
}
