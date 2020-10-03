package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.core.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.core.utils.CountingIdlingResourceProvider
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_running_records.interactor.RunningRecordsViewDataInteractor
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordTypeAddViewData
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.ChangeRunningRecordParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class RunningRecordsViewModel @Inject constructor(
    private val router: Router,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val runningRecordsViewDataInteractor: RunningRecordsViewDataInteractor
) : ViewModel() {

    val runningRecords: LiveData<List<ViewHolderType>> by lazy {
        startUpdate()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    private var timerJob: Job? = null

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (!prefsInteractor.getAllowMultitasking()) {
                runningRecordInteractor.getAll()
                    .filter { it.id != item.id }
                    .forEach { handleRunningRecordRemove(it) }
            }
            addRunningRecordMediator.add(item.id)
            updateRunningRecords()
        }
    }

    fun onRecordTypeLongClick(item: RecordTypeViewData, sharedElements: Map<Any, String>) {
        router.navigate(
            screen = Screen.CHANGE_RECORD_TYPE,
            data = ChangeRecordTypeParams(item.id, item.width, item.height, item.asRow),
            sharedElements = sharedElements
        )
    }

    fun onAddRecordTypeClick(item: RunningRecordTypeAddViewData) {
        router.navigate(
            screen = Screen.CHANGE_RECORD_TYPE,
            data = ChangeRecordTypeParams(0, item.width, item.height, item.asRow)
        )
    }

    fun onRunningRecordClick(item: RunningRecordViewData) {
        viewModelScope.launch {
            runningRecordInteractor.get(item.id)
                ?.let { handleRunningRecordRemove(it) }
            updateRunningRecords()
        }
    }

    fun onRunningRecordLongClick(item: RunningRecordViewData, sharedElements: Map<Any, String>) {
        router.navigate(
            screen = Screen.CHANGE_RECORD_RUNNING,
            data = ChangeRunningRecordParams(item.id),
            sharedElements = sharedElements
        )
    }

    fun onVisible() {
        startUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    private fun updateRunningRecords() = viewModelScope.launch {
        CountingIdlingResourceProvider.increment()
        (runningRecords as MutableLiveData).value = loadRunningRecordsViewData()
        CountingIdlingResourceProvider.decrement()
    }

    private suspend fun loadRunningRecordsViewData(): List<ViewHolderType> {
        return runningRecordsViewDataInteractor.getViewData()
    }

    private suspend fun handleRunningRecordRemove(runningRecord: RunningRecord) {
        recordInteractor.add(
            typeId = runningRecord.id,
            timeStarted = runningRecord.timeStarted
        )
        removeRunningRecordMediator.remove(runningRecord.id)
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
