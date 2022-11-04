package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.core.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.interactor.RunningRecordsViewDataInteractor
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordTypeAddViewData
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
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
    private val runningRecordsViewDataInteractor: RunningRecordsViewDataInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
) : ViewModel() {

    val runningRecords: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    private var timerJob: Job? = null

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            addRunningRecordMediator.tryStartTimer(
                typeId = item.id,
                onNeedToShowTagSelection = { showTagSelection(item.id) }
            )
            updateRunningRecords()
        }
    }

    fun onRecordTypeLongClick(item: RecordTypeViewData, sharedElements: Map<Any, String>) {
        router.navigate(
            data = ChangeRecordTypeParams.Change(
                transitionName = TransitionNames.RECORD_TYPE + item.id,
                id = item.id,
                sizePreview = ChangeRecordTypeParams.SizePreview(
                    width = item.width,
                    height = item.height,
                    asRow = item.asRow
                ),
                preview = ChangeRecordTypeParams.Change.Preview(
                    name = item.name,
                    iconId = item.iconId.toParams(),
                    color = item.color
                )
            ),
            sharedElements = sharedElements
        )
    }

    fun onAddRecordTypeClick(item: RunningRecordTypeAddViewData) {
        router.navigate(
            data = ChangeRecordTypeParams.New(
                sizePreview = ChangeRecordTypeParams.SizePreview(
                    width = item.width,
                    height = item.height,
                    asRow = item.asRow
                )
            )
        )
    }

    fun onRunningRecordClick(item: RunningRecordViewData) {
        viewModelScope.launch {
            runningRecordInteractor.get(item.id)
                ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
            updateRunningRecords()
        }
    }

    fun onRunningRecordLongClick(item: RunningRecordViewData, sharedElements: Map<Any, String>) {
        router.navigate(
            data = ChangeRunningRecordParams(
                id = item.id,
                preview = ChangeRunningRecordParams.Preview(
                    name = item.name,
                    tagName = item.tagName,
                    timeStarted = item.timeStarted,
                    duration = item.timer,
                    goalTime = item.goalTime,
                    iconId = item.iconId.toParams(),
                    color = item.color,
                    comment = item.comment
                )
            ),
            sharedElements = sharedElements
        )
    }

    fun onActivityFilterClick(item: ActivityFilterViewData) = viewModelScope.launch {
        activityFilterInteractor.changeSelected(item.id, !item.selected)
        updateRunningRecords()
    }

    fun onActivityFilterLongClick(item: ActivityFilterViewData, sharedElements: Pair<Any, String>) {
        router.navigate(
            data = ChangeActivityFilterParams.Change(
                transitionName = sharedElements.second,
                id = item.id,
                preview = ChangeActivityFilterParams.Change.Preview(
                    name = item.name,
                    color = item.color
                )
            ),
            sharedElements = sharedElements.let(::mapOf)
        )
    }

    fun onAddActivityFilterClick() = viewModelScope.launch {
        router.navigate(
            data = ChangeActivityFilterParams.New,
        )
    }

    fun onVisible() {
        startUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    fun onTagSelected() {
        updateRunningRecords()
    }

    private fun showTagSelection(typeId: Long) {
        router.navigate(RecordTagSelectionParams(typeId))
    }

    private fun updateRunningRecords() = viewModelScope.launch {
        val data = loadRunningRecordsViewData()
        (runningRecords as MutableLiveData).value = data
    }

    private suspend fun loadRunningRecordsViewData(): List<ViewHolderType> {
        return runningRecordsViewDataInteractor.getViewData()
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
