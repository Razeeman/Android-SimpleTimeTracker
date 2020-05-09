package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_records.adapter.RecordViewData
import com.example.util.simpletimetracker.feature_records.mapper.RecordViewDataMapper
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsViewModel : ViewModel() {

    @Inject
    lateinit var recordInteractor: RecordInteractor
    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor
    @Inject
    lateinit var recordViewDataMapper: RecordViewDataMapper

    private val recordsLiveData: MutableLiveData<List<RecordViewData>> by lazy {
        return@lazy MutableLiveData<List<RecordViewData>>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordsViewData() }
            initial
        }
    }

    val records: LiveData<List<RecordViewData>>
        get() = recordsLiveData

    fun clearRecordTypes() {
        viewModelScope.launch {
            recordInteractor.clear()
            updateRecords()
        }
    }

    fun onRecordClick(item: RecordViewData) {
        viewModelScope.launch {
            recordInteractor.remove(item.id)
            updateRecords()
        }
    }

    fun onVisible() {
        viewModelScope.launch {
            updateRecords()
        }
    }

    private suspend fun updateRecords() {
        recordsLiveData.value = loadRecordsViewData()
    }

    private suspend fun loadRecordsViewData(): List<RecordViewData> {
        val recordTypes = recordTypeInteractor.getAll()
            .map { it.id to it }
            .toMap()
        val records = recordInteractor.getAll()

        return records
            .mapNotNull { record ->
                recordTypes[record.typeId]?.let { type -> record to type }
            }
            .map { (record, recordType) ->
                recordViewDataMapper.map(record, recordType)
            }
    }
}
