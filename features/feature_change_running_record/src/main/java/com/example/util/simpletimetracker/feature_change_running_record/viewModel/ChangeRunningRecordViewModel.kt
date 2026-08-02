package com.example.util.simpletimetracker.feature_change_running_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordsInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.interactor.AddTagToTypeIfNotExistMediator
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_change_record.api.ChangeRecordConfig
import com.example.util.simpletimetracker.feature_change_record.api.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.api.ChangeRecordEditorDelegate
import com.example.util.simpletimetracker.feature_change_record.api.ChangeRecordEditorMode
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordEditorState
import com.example.util.simpletimetracker.feature_change_running_record.R
import com.example.util.simpletimetracker.feature_change_running_record.interactor.ChangeRunningRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_running_record.mapper.ChangeRunningRecordMapper
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeRunningRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val editorDelegate: ChangeRecordEditorDelegate,
    private val router: Router,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val changeRunningRecordViewDataInteractor: ChangeRunningRecordViewDataInteractor,
    private val resourceRepo: ResourceRepo,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val changeRunningRecordMapper: ChangeRunningRecordMapper,
    private val updateRunningRecordsInteractor: UpdateRunningRecordsInteractor,
    private val addTagToTypeIfNotExistMediator: AddTagToTypeIfNotExistMediator,
) : BaseViewModel() {

    private val extra: ChangeRunningRecordParams = savedStateHandle[ARGS_PARAMS]
        ?: ChangeRunningRecordParams.Empty

    private val mode: ChangeRecordEditorMode = ChangeRecordEditorMode(
        config = ChangeRecordConfig(
            forceSecondsInDurationDialog = true,
            showTimeEndedOnSplitPreview = false,
            showTimeEndedOnAdjustPreview = false,
            adjustNextRecordAvailable = false,
            isTimeEndedAvailable = false,
            isAdditionalActionsAvailable = false,
            isDuplicateActionAvailable = true,
            isDeleteButtonVisible = true,
            isStatisticsButtonVisible = true,
        ),
        mergeAvailable = { false },
        previewTimeEnded = { System.currentTimeMillis() },
        adjustPreviewTimeEnded = { System.currentTimeMillis() },
        adjustPreviewOriginalTimeEnded = { System.currentTimeMillis() },
        updatePreview = ::updatePreview,
        getChangeCategoryParams = ::getChangeCategoryParams,
        onSaveClickDelegate = ::onSaveClickDelegate,
        sendPreviewUpdate = ::sendPreviewUpdate,
        initializePreviewViewData = ::initializePreviewViewData,
        onDeleteClick = ::onDeleteClickMode,
        onStatisticsClick = ::onStatisticsClickMode,
        onTimeStartedChanged = ::onTimeStartedChanged,
        onTimeEndedChanged = {},
    )

    val record: LiveData<ChangeRunningRecordViewData> by lazy {
        return@lazy MutableLiveData<ChangeRunningRecordViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadPreviewViewData()
            }
            initial
        }
    }
    val message: LiveData<SnackBarParams?> = MutableLiveData()
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    private var timerJob: Job? = null

    init {
        editorDelegate.attach(mode)
    }

    override fun onCleared() {
        editorDelegate.clear()
        super.onCleared()
    }

    fun onVisible() {
        editorDelegate.onVisible()
        startUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    fun onMessageShown() {
        message.set(null)
    }

    private fun onDeleteClickMode() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            removeRunningRecordMediator.remove(extra.id)
            editorDelegate.showMessage(R.string.change_running_record_removed)
            router.back()
        }
    }

    private fun onStatisticsClickMode() = viewModelScope.launch {
        val preview = record.value?.recordPreview ?: return@launch

        statisticsDetailNavigationInteractor.navigate(
            transitionName = "",
            filterType = ChartFilterType.ACTIVITY,
            shift = 0,
            overrideStatisticsRange = null,
            sharedElements = emptyMap(),
            itemId = editorDelegate.recordState.newTypeId,
            itemName = preview.name,
            itemIcon = preview.iconId,
            itemColor = preview.color,
        )
    }

    private suspend fun onSaveClickDelegate(
        doAfter: suspend () -> Unit,
    ) {
        val recordState = editorDelegate.recordState
        // Widgets will update on adding.
        removeRunningRecordMediator.remove(
            typeId = extra.id,
            updateWidgets = false,
            updateNotificationSwitch = false,
            checkPomodoroStop = extra.id != recordState.newTypeId,
        )
        addRunningRecordMediator.addAfterChange(
            typeId = recordState.newTypeId,
            timeStarted = recordState.newTimeStarted,
            comment = editorDelegate.commentSelectionViewModelDelegate.newComment,
            tags = recordState.newTags,
        )
        addTagToTypeIfNotExistMediator.execute(
            typeId = recordState.newTypeId,
            tagIds = recordState.newTags.map(RecordBase.Tag::tagId),
        )
        doAfter()
        sendPreviewUpdate(fullUpdate = true)
        router.back()
    }

    private suspend fun sendPreviewUpdate(fullUpdate: Boolean) {
        val recordPreview = record.value?.recordPreview ?: return
        val update = changeRunningRecordMapper.map(
            fullUpdate = fullUpdate,
            recordPreview = recordPreview,
        )
        updateRunningRecordsInteractor.send(update)
    }

    private fun getChangeCategoryParams(data: ChangeTagData): ChangeRecordTagFromScreen {
        return ChangeRecordTagFromChangeRunningRecordParams(data)
    }

    private suspend fun onTimeStartedChanged() {
        val recordState = editorDelegate.recordState
        if (recordState.newTimeStarted > System.currentTimeMillis()) {
            recordState.newTimeStarted = System.currentTimeMillis()

            SnackBarParams(
                message = resourceRepo.getString(R.string.cannot_be_in_the_future),
                duration = SnackBarParams.Duration.Short,
            ).let(message::set)
        }
        editorDelegate.afterTimeStartedChanged()
    }

    private fun mapRecordModel(
        comment: String,
        recordState: ChangeRecordEditorState,
    ): RunningRecord {
        return RunningRecord(
            id = recordState.newTypeId,
            timeStarted = recordState.newTimeStarted,
            comment = comment,
            tags = recordState.newTags,
        )
    }

    private suspend fun updatePreview() {
        record.set(loadPreviewViewData())
    }

    private suspend fun initializePreviewViewData() {
        val recordState = editorDelegate.recordState
        if (extra.id != 0L) {
            runningRecordInteractor.get(extra.id)?.let { record ->
                recordState.newTypeId = record.id.orZero()
                recordState.newTimeStarted = record.timeStarted
                recordState.newTimeEnded = System.currentTimeMillis()
                recordState.newTags = record.tags.toMutableList()
                editorDelegate.commentSelectionViewModelDelegate.newComment = record.comment
            }
            editorDelegate.afterInitializePreviewViewData()
        }
    }

    private suspend fun loadPreviewViewData(): ChangeRunningRecordViewData {
        val recordState = editorDelegate.recordState
        if (recordState.newTypeId == 0L) initializePreviewViewData()

        val record = mapRecordModel(
            comment = editorDelegate.commentSelectionViewModelDelegate.newComment,
            recordState = recordState,
        )

        return changeRunningRecordViewDataInteractor.getPreviewViewData(
            record = record,
            params = extra,
            dateTimeFieldState = editorDelegate.dateTimeState,
        )
    }

    private fun startUpdate() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                updatePreview()
                // Update split preview only if it is visible
                if (editorDelegate.chooserState.value?.current is ChangeRecordChooserState.State.Action) {
                    editorDelegate.updateActionsData()
                }
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        timerJob?.cancel()
    }

    companion object {
        private const val TIMER_UPDATE = 1000L
    }
}