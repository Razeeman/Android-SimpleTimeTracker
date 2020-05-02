package com.example.util.simpletimetracker.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.TimePeriod
import com.example.util.simpletimetracker.domain.TimePeriodInteractor
import com.example.util.simpletimetracker.domain.orTrue
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel : ViewModel() {

    @Inject
    lateinit var timerPeriodInteractor: TimePeriodInteractor

    private val periodsLiveData: MutableLiveData<List<TimePeriod>> = MutableLiveData()

    fun getPeriods(): LiveData<List<TimePeriod>> {
        if (periodsLiveData.value?.isEmpty().orTrue()) {
            viewModelScope.launch {
                update()
            }
        }
        return periodsLiveData
    }

    fun add() {
        val period = TimePeriod(
            name = "name" + (0..10).random(),
            timeStarted = (0..100L).random(),
            timeEnded = (100..200L).random()
        )

        viewModelScope.launch {
            timerPeriodInteractor.add(period)
            update()
        }
    }

    fun clear() {
        viewModelScope.launch {
            timerPeriodInteractor.clear()
            update()
        }
    }

    private suspend fun update() {
        periodsLiveData.postValue(load())
    }

    private suspend fun load(): List<TimePeriod> {
        return timerPeriodInteractor.getAll()
    }
}
