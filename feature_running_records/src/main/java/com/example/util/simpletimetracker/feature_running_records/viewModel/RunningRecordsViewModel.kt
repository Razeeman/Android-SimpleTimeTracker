package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
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
    lateinit var recordTypeViewDataMapper: RecordTypeViewDataMapper
    @Inject
    lateinit var runningRecordViewDataMapper: RunningRecordViewDataMapper

    private val random = Random(1)

    private val runningRecordsLiveData: MutableLiveData<List<RunningRecordViewData>> =
        MutableLiveData()
    private val recordTypesLiveData: MutableLiveData<List<RecordTypeViewData>> by lazy {
        return@lazy MutableLiveData<List<RecordTypeViewData>>().let { initial ->
            viewModelScope.launch { initial.value = load() }
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
            update()
        }
    }

    fun clearRecordTypes() {
        viewModelScope.launch {
            recordTypeInteractor.clear()
            update()
        }
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        val record = RunningRecord(
            name = item.name,
            timeStarted = System.currentTimeMillis()
        )

        listOf(record.let(runningRecordViewDataMapper::map))
            .let(runningRecordsLiveData::setValue)
    }

    private suspend fun update() {
        recordTypesLiveData.value = load()
    }

    private suspend fun load(): List<RecordTypeViewData> {
        return recordTypeInteractor
            .getAll()
            .map(recordTypeViewDataMapper::map)
    }
}
