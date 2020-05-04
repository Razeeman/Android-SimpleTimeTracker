package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_running_records.adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_running_records.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordViewDataMapper
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

class RunningRecordsViewModel : ViewModel() {

    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor
    @Inject
    lateinit var runningRecordInteractor: RunningRecordInteractor
    @Inject
    lateinit var recordTypeViewDataMapper: RecordTypeViewDataMapper
    @Inject
    lateinit var runningRecordViewDataMapper: RunningRecordViewDataMapper

    private val random = Random(1)

    private val runningRecordsLiveData: MutableLiveData<List<RunningRecordViewData>> =
        MutableLiveData()
    private val recordTypesLiveData: MutableLiveData<List<RecordTypeViewData>> by lazy {
        return@lazy MutableLiveData<List<RecordTypeViewData>>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTypes() }
            initial
        }
    }

    val runningRecords: LiveData<List<RunningRecordViewData>>
        get() = runningRecordsLiveData
    val recordTypes: LiveData<List<RecordTypeViewData>>
        get() = recordTypesLiveData

    fun addRecordType() {
        val recordType = RecordType(
            name = "name" + (0..10).random(),
            color = random.nextInt()
        )

        viewModelScope.launch {
            recordTypeInteractor.add(recordType)
            updateRecordTypes()
        }
    }

    fun clearRecordTypes() {
        viewModelScope.launch {
            recordTypeInteractor.clear()
            updateRecordTypes()
        }
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        val record = RunningRecord(
            name = item.name,
            timeStarted = System.currentTimeMillis()
        )

        viewModelScope.launch {
            runningRecordInteractor.add(record)
            updateRunningRecords()
        }
    }

    fun onRunningRecordClick(item: RunningRecordViewData) {
        viewModelScope.launch {
            runningRecordInteractor.remove(item.id)
            updateRunningRecords()
        }
    }

    private suspend fun updateRunningRecords() {
        runningRecordsLiveData.value = loadRunningRecords()
    }

    private suspend fun updateRecordTypes() {
        recordTypesLiveData.value = loadRecordTypes()
    }

    private suspend fun loadRunningRecords(): List<RunningRecordViewData> {
        return runningRecordInteractor
            .getAll()
            .map(runningRecordViewDataMapper::map)
    }

    private suspend fun loadRecordTypes(): List<RecordTypeViewData> {
        return recordTypeInteractor
            .getAll()
            .map(recordTypeViewDataMapper::map)
    }

    private fun startUpdate() {
        // TODO
    }
}
