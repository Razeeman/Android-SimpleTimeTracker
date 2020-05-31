package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.feature_running_records.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.feature_running_records.viewData.*
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordTypeParams
import kotlinx.coroutines.*
import javax.inject.Inject

class RunningRecordsViewModel @Inject constructor(
    private val router: Router,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val runningRecordViewDataMapper: RunningRecordViewDataMapper
) : ViewModel() {

    val runningRecords: LiveData<List<ViewHolderType>> by lazy {
        startUpdate()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        updateRecordTypes()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    private var timerJob: Job? = null

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            runningRecordInteractor.add(item.id)
            widgetInteractor.updateWidgets()
            updateRunningRecords()
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
                runningRecordInteractor.remove(item.id)
                widgetInteractor.updateWidgets()
            }
            updateRunningRecords()
        }
    }

    fun onVisible() {
        updateRecordTypes()
        startUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    private fun updateRunningRecords() = viewModelScope.launch {
        (runningRecords as MutableLiveData).value = loadRunningRecordsViewData()
    }

    private fun updateRecordTypes() = viewModelScope.launch {
        (recordTypes as MutableLiveData).value = loadRecordTypesViewData()
    }

    private suspend fun loadRunningRecordsViewData(): List<ViewHolderType> {
        val recordTypes = recordTypeInteractor.getAll()
            .map { it.id to it }
            .toMap()
        val runningRecords = runningRecordInteractor.getAll()

        if (recordTypes.isEmpty()) return emptyList()
        if (runningRecords.isEmpty()) return listOf(runningRecordViewDataMapper.mapToEmpty())

        return runningRecords
            .sortedByDescending {
                it.timeStarted
            }
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
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updateRunningRecords()
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
        }
    }

    companion object {
        private const val TIMER_UPDATE = 1000L
    }
}
