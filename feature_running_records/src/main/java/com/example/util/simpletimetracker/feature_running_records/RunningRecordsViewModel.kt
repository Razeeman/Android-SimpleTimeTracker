package com.example.util.simpletimetracker.feature_running_records

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.Record
import kotlinx.coroutines.launch
import javax.inject.Inject

class RunningRecordsViewModel : ViewModel() {

    @Inject
    lateinit var recordInteractor: RecordInteractor

    private val recordsLiveData: MutableLiveData<List<Record>> by lazy {
        return@lazy MutableLiveData<List<Record>>().let { initial ->
            viewModelScope.launch { initial.value = load() }
            initial
        }
    }

    val records: LiveData<List<Record>> get() = recordsLiveData

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
        recordsLiveData.value = load()
    }

    private suspend fun load(): List<Record> {
        return recordInteractor.getAll()
    }
}
