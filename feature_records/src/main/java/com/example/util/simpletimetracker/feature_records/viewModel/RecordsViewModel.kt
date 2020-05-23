package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_records.adapter.RecordViewData
import com.example.util.simpletimetracker.feature_records.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsViewModel(
    private val start: Long,
    private val end: Long
) : ViewModel() {

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
    @Inject
    lateinit var timeMapper: TimeMapper

    val records: LiveData<List<RecordViewData>> by lazy {
        return@lazy MutableLiveData<List<RecordViewData>>().apply {
            viewModelScope.launch { value = loadRecordsViewData() }
        }
    }

    fun onRecordClick(item: RecordViewData) {
        router.navigate(Screen.CHANGE_RECORD, ChangeRecordParams(item.id))
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
        val records = if (start != 0L && end != 0L) {
            recordInteractor.getFromRange(start, end)
        } else {
            recordInteractor.getAll()
        }

        return records
            .mapNotNull { record ->
                recordTypes[record.typeId]?.let { type -> record to type }
            }
            .sortedByDescending { (record, _) ->
                record.timeStarted
            }
            .map { (record, recordType) ->
                recordViewDataMapper.map(record, recordType, start, end)
            }
    }
}
