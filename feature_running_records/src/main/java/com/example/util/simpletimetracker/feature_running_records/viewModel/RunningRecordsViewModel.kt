package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_running_records.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.feature_running_records.viewData.RecordTypeAddViewData
import com.example.util.simpletimetracker.feature_running_records.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordEmptyViewData
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordTypeParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class RunningRecordsViewModel : ViewModel() {

    @Inject
    lateinit var router: Router
    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor
    @Inject
    lateinit var runningRecordInteractor: RunningRecordInteractor
    @Inject
    lateinit var recordInteractor: RecordInteractor
    @Inject
    lateinit var recordTypeViewDataMapper: RecordTypeViewDataMapper
    @Inject
    lateinit var runningRecordViewDataMapper: RunningRecordViewDataMapper

    val runningRecords: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadRunningRecordsViewData() }
            startUpdate()
            initial
        }
    }
    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTypesViewData() }
            initial
        }
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (runningRecordInteractor.get(item.id) == null) {
                runningRecordInteractor.add(item.id)
                updateRunningRecords()
            }
        }
    }

    fun onRecordTypeLongClick(item: RecordTypeViewData) {
        router.navigate(Screen.CHANGE_RECORD_TYPE, ChangeRecordTypeParams(item.id))
    }

    fun onAddRecordTypeClick() {
        router.navigate(Screen.CHANGE_RECORD_TYPE)
    }

    fun onRunningRecordClick(item: RunningRecordViewData) {
        viewModelScope.launch {
            runningRecordInteractor.get(item.id)?.let { runningRecord ->
                recordInteractor.add(
                    typeId = runningRecord.id,
                    timeStarted = runningRecord.timeStarted
                )
            }
            runningRecordInteractor.remove(item.id)
            updateRunningRecords()
        }
    }

    fun onVisible() {
        viewModelScope.launch {
            updateRunningRecords()
            updateRecordTypes()
        }
    }

    private suspend fun updateRunningRecords() {
        (runningRecords as MutableLiveData).value = loadRunningRecordsViewData()
    }

    private suspend fun updateRecordTypes() {
        (recordTypes as MutableLiveData).value = loadRecordTypesViewData()
    }

    private suspend fun loadRunningRecordsViewData(): List<ViewHolderType> {
        val recordTypes = recordTypeInteractor.getAll()
            .map { it.id to it }
            .toMap()
        val runningRecords = runningRecordInteractor.getAll()

        if (recordTypes.isEmpty()) return emptyList()
        if (runningRecords.isEmpty()) return listOf(RunningRecordEmptyViewData())

        return runningRecords
            .mapNotNull { runningRecord ->
                recordTypes[runningRecord.id]?.let { type -> runningRecord to type }
            }
            .map { (runningRecord, recordType) ->
                runningRecordViewDataMapper.map(runningRecord, recordType)
            }
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        return recordTypeInteractor
            .getAll()
            .filter { !it.hidden }
            .map(recordTypeViewDataMapper::map) + RecordTypeAddViewData()
    }

    private fun startUpdate() {
        viewModelScope.launch {
            delay(TIMER_UPDATE)
            while (true) {
                delay(TIMER_UPDATE)
                updateRunningRecords()
            }
        }
    }

    companion object {
        private const val TIMER_UPDATE = 1000L
    }
}
