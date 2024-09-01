package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.extension.toRecordParams
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.core.mapper.ChangeRecordDateTimeMapper
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.ChangeSelectedActivityFilterMediator
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.UpdateRunningRecordFromChangeScreenInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.RunningRecordTypeSpecialViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_running_records.interactor.RunningRecordsViewDataInteractor
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.DefaultTypesSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.PomodoroParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunningRecordsViewModel @Inject constructor(
    private val router: Router,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordRepeatInteractor: RecordRepeatInteractor,
    private val runningRecordsViewDataInteractor: RunningRecordsViewDataInteractor,
    private val changeSelectedActivityFilterMediator: ChangeSelectedActivityFilterMediator,
    private val prefsInteractor: PrefsInteractor,
    private val updateRunningRecordFromChangeScreenInteractor: UpdateRunningRecordFromChangeScreenInteractor,
    private val changeRecordDateTimeMapper: ChangeRecordDateTimeMapper,
) : ViewModel() {

    val runningRecords: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()
    val previewUpdate: SingleLiveEvent<UpdateRunningRecordFromChangeScreenInteractor.Update> = SingleLiveEvent()

    private var timerJob: Job? = null

    init {
        subscribeToUpdates()
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            val runningRecord = runningRecordInteractor.get(item.id)

            if (runningRecord != null) {
                // Stop running record, add new record
                removeRunningRecordMediator.removeWithRecordAdd(runningRecord)
            } else {
                // Start running record
                addRunningRecordMediator.tryStartTimer(
                    typeId = item.id,
                    onNeedToShowTagSelection = { showTagSelection(item.id) },
                )
            }
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
                    asRow = item.asRow,
                ),
                preview = ChangeRecordTypeParams.Change.Preview(
                    name = item.name,
                    iconId = item.iconId.toParams(),
                    color = item.color,
                ),
            ),
            sharedElements = sharedElements,
        )
    }

    fun onSpecialRecordTypeClick(item: RunningRecordTypeSpecialViewData) {
        when (item.type) {
            is RunningRecordTypeSpecialViewData.Type.Add -> {
                router.navigate(
                    data = ChangeRecordTypeParams.New(
                        sizePreview = ChangeRecordTypeParams.SizePreview(
                            width = item.width,
                            height = item.height,
                            asRow = item.asRow,
                        ),
                    ),
                )
            }
            is RunningRecordTypeSpecialViewData.Type.Default -> {
                router.navigate(
                    data = DefaultTypesSelectionDialogParams,
                )
            }
            is RunningRecordTypeSpecialViewData.Type.Repeat -> viewModelScope.launch {
                recordRepeatInteractor.repeat()
            }
            is RunningRecordTypeSpecialViewData.Type.Pomodoro -> {
                router.navigate(PomodoroParams)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRunningRecordClick(item: RunningRecordViewData, sharedElements: Pair<Any, String>) {
        viewModelScope.launch {
            runningRecordInteractor.get(item.id)
                ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
            updateRunningRecords()
        }
    }

    fun onRunningRecordLongClick(
        item: RunningRecordViewData,
        sharedElements: Pair<Any, String>,
    ) = viewModelScope.launch {
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        val params = ChangeRunningRecordParams(
            transitionName = sharedElements.second,
            id = item.id,
            from = ChangeRunningRecordParams.From.RunningRecords,
            preview = ChangeRunningRecordParams.Preview(
                name = item.name,
                tagName = item.tagName,
                timeStarted = item.timeStarted,
                timeStartedDateTime = changeRecordDateTimeMapper.map(
                    param = ChangeRecordDateTimeMapper.Param.DateTime(item.timeStartedTimestamp),
                    field = ChangeRecordDateTimeMapper.Field.Start,
                    useMilitaryTimeFormat = useMilitaryTimeFormat,
                    showSeconds = showSeconds,
                ).toRecordParams(),
                duration = item.timer,
                durationTotal = item.timerTotal,
                goalTime = item.goalTime.toParams(),
                iconId = item.iconId.toParams(),
                color = item.color,
                comment = item.comment,
            ),
        )
        router.navigate(
            ChangeRunningRecordFromMainParams(params = params),
            sharedElements = sharedElements.let(::mapOf),
        )
    }

    fun onActivityFilterClick(item: ActivityFilterViewData) {
        viewModelScope.launch {
            changeSelectedActivityFilterMediator.onFilterClicked(item.id, item.selected)
            updateRunningRecords()
        }
    }

    fun onActivityFilterLongClick(item: ActivityFilterViewData, sharedElements: Pair<Any, String>) {
        router.navigate(
            data = ChangeActivityFilterParams.Change(
                transitionName = sharedElements.second,
                id = item.id,
                preview = ChangeActivityFilterParams.Change.Preview(
                    name = item.name,
                    color = item.color,
                ),
            ),
            sharedElements = sharedElements.let(::mapOf),
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

    fun onTabReselected(tab: NavigationTab?) {
        if (tab is NavigationTab.RunningRecords) {
            resetScreen.set(Unit)
        }
    }

    private fun showTagSelection(typeId: Long) {
        router.navigate(RecordTagSelectionParams(typeId))
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            updateRunningRecordFromChangeScreenInteractor.dataUpdated.collect {
                onUpdateReceived(it)
            }
        }
    }

    private fun onUpdateReceived(
        update: UpdateRunningRecordFromChangeScreenInteractor.Update,
    ) {
        previewUpdate.set(update)
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
