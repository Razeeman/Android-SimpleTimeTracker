package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordDividerViewData
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordTypeParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

    private var timerJob: Job? = null

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            runningRecordInteractor.add(item.id)
            widgetInteractor.updateWidgets()
            updateRunningRecords()
        }
    }

    fun onRecordTypeLongClick(item: RecordTypeViewData, sharedElements: Map<Any, String>) {
        router.navigate(
            screen = Screen.CHANGE_RECORD_TYPE,
            data = ChangeRecordTypeParams(item.id),
            sharedElements = sharedElements
        )
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
        startUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    private fun updateRunningRecords() = viewModelScope.launch {
        (runningRecords as MutableLiveData).value = loadRunningRecordsViewData()
    }

    private suspend fun loadRunningRecordsViewData(): List<ViewHolderType> {
        val recordTypes = recordTypeInteractor.getAll()
        val recordTypesMap = recordTypes
            .map { it.id to it }
            .toMap()
        val runningRecords = runningRecordInteractor.getAll()

        val runningRecordsViewData = when {
            recordTypes.isEmpty() -> emptyList()
            runningRecords.isEmpty() -> listOf(runningRecordViewDataMapper.mapToEmpty())
            else -> runningRecords
                .sortedByDescending {
                    it.timeStarted
                }
                .mapNotNull { runningRecord ->
                    recordTypesMap[runningRecord.id]?.let { type -> runningRecord to type }
                }
                .map { (runningRecord, recordType) ->
                    runningRecordViewDataMapper.map(runningRecord, recordType)
                }
        }

        val recordTypesViewData = recordTypes
            .filter { !it.hidden }
            .map(recordTypeViewDataMapper::map) + runningRecordViewDataMapper.mapToAddItem()

        return runningRecordsViewData +
            listOf(RunningRecordDividerViewData) +
            recordTypesViewData
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
