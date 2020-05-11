package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_running_records.adapter.recordType.RecordTypeAddViewData
import com.example.util.simpletimetracker.feature_running_records.adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.feature_running_records.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordViewDataMapper
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
    lateinit var resourceRepo: ResourceRepo
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
    @Inject
    lateinit var colorMapper: ColorMapper

    private val runningRecordsLiveData: MutableLiveData<List<RunningRecordViewData>> by lazy {
        return@lazy MutableLiveData<List<RunningRecordViewData>>().let { initial ->
            viewModelScope.launch { initial.value = loadRunningRecordsViewData() }
            startUpdate()
            initial
        }
    }
    private val recordTypesLiveData: MutableLiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTypesViewData() }
            initial
        }
    }

    val runningRecords: LiveData<List<RunningRecordViewData>>
        get() = runningRecordsLiveData
    val recordTypes: LiveData<List<ViewHolderType>>
        get() = recordTypesLiveData

    fun onRecordTypeClick(item: RecordTypeViewData) {
        val record = RunningRecord(
            id = item.id,
            timeStarted = System.currentTimeMillis()
        )

        viewModelScope.launch {
            runningRecordInteractor.add(record)
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
            val runningRecord = runningRecordInteractor.getAll() // TODO get by name
                .firstOrNull {
                    it.id == item.id
                }
            runningRecord?.let {
                recordInteractor.add(
                    Record(
                        typeId = runningRecord.id,
                        timeStarted = runningRecord.timeStarted,
                        timeEnded = System.currentTimeMillis()
                    )
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
        runningRecordsLiveData.value = loadRunningRecordsViewData()
    }

    private suspend fun updateRecordTypes() {
        recordTypesLiveData.value = loadRecordTypesViewData()
    }

    private suspend fun loadRunningRecordsViewData(): List<RunningRecordViewData> {
        val recordTypes = recordTypeInteractor.getAll()
            .map { it.id to it }
            .toMap()
        val runningRecords = runningRecordInteractor.getAll()

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
