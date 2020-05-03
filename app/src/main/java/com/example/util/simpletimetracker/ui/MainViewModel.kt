package com.example.util.simpletimetracker.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.Record
import com.example.util.simpletimetracker.domain.RecordInteractor
import com.example.util.simpletimetracker.domain.orTrue
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel : ViewModel() {

    @Inject
    lateinit var recordInteractor: RecordInteractor

    private val recordsLiveData: MutableLiveData<List<Record>> = MutableLiveData()

    fun getRecords(): LiveData<List<Record>> {
        if (recordsLiveData.value?.isEmpty().orTrue()) {
            viewModelScope.launch {
                update()
            }
        }
        return recordsLiveData
    }

    fun add() {
        val record = Record(
            name = "name" + (0..10).random(),
            timeStarted = (0..100L).random(),
            timeEnded = (100..200L).random()
        )

        viewModelScope.launch {
            recordInteractor.add(record)
            update()
        }
    }

    fun clear() {
        viewModelScope.launch {
            recordInteractor.clear()
            update()
        }
    }

    private suspend fun update() {
        recordsLiveData.postValue(load())
    }

    private suspend fun load(): List<Record> {
        return recordInteractor.getAll()
    }
}
