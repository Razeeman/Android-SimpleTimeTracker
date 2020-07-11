package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.utils.CountingIdlingResourceProvider
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.feature_records.viewData.RecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsViewModel @Inject constructor(
    private val router: Router,
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordViewDataMapper: RecordViewDataMapper
) : ViewModel() {

    lateinit var extra: RecordsExtra

    val records: LiveData<List<ViewHolderType>> by lazy {
        updateRecords()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    fun onRecordClick(item: RecordViewData, sharedElements: Map<Any, String>) {
        router.navigate(
            screen = Screen.CHANGE_RECORD,
            data = ChangeRecordParams(item.id),
            sharedElements = sharedElements
        )
    }

    fun onVisible() {
        updateRecords()
    }

    fun onNeedUpdate() {
        updateRecords()
    }

    private fun updateRecords() = viewModelScope.launch {
        CountingIdlingResourceProvider.increment()
        (records as MutableLiveData).value = loadRecordsViewData()
        CountingIdlingResourceProvider.decrement()
    }

    private suspend fun loadRecordsViewData(): List<ViewHolderType> {
        val recordTypes = recordTypeInteractor.getAll()
            .map { it.id to it }
            .toMap()
        val records = if (extra.rangeStart != 0L && extra.rangeEnd != 0L) {
            recordInteractor.getFromRange(extra.rangeStart, extra.rangeEnd)
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
                recordViewDataMapper.map(record, recordType, extra.rangeStart, extra.rangeEnd)
            }
            .ifEmpty {
                return listOf(recordViewDataMapper.mapToEmpty())
            }
    }
}
