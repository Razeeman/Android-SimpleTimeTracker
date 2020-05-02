package com.example.util.simpletimetracker.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.util.simpletimetracker.domain.TimePeriod
import com.example.util.simpletimetracker.domain.TimePeriodInteractor
import javax.inject.Inject

class MainViewModel : ViewModel() {

    @Inject
    lateinit var timerPeriodInteractor: TimePeriodInteractor

    private val periodsLiveData: LiveData<List<TimePeriod>> = liveData {
        val data = timerPeriodInteractor.getAll()
        emit(data)
    }

    val periods: LiveData<List<TimePeriod>> get() = periodsLiveData
}
