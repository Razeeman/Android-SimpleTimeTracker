package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.adapter.RecordViewData
import com.example.util.simpletimetracker.feature_records.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.navigation.Router
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsViewModel : ViewModel() {

    @Inject
    lateinit var router: Router
    @Inject
    lateinit var resourceRepo: ResourceRepo
    @Inject
    lateinit var recordInteractor: RecordInteractor
    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor
    @Inject
    lateinit var recordViewDataMapper: RecordViewDataMapper

    val records: LiveData<List<RecordViewData>> by lazy {
        return@lazy MutableLiveData<List<RecordViewData>>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordsViewData() }
            initial
        }
    }

    fun onRecordClick(item: RecordViewData) {
        viewModelScope.launch {
            recordInteractor.remove(item.id)
            updateRecords()
            resourceRepo.getString(R.string.record_removed)
                .let(router::showSystemMessage)
        }
    }

    fun onVisible() {
        viewModelScope.launch {
            updateRecords()
        }
    }

    private suspend fun updateRecords() {
        (records as MutableLiveData).value = loadRecordsViewData()
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
