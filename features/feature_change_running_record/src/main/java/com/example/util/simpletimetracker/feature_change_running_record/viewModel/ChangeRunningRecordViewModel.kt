package com.example.util.simpletimetracker.feature_change_running_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordsInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.interactor.AddTagToTypeIfNotExistMediator
import com.example.util.simpletimetracker.domain.recordTag.interactor.NeedTagValueSelectionInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordActionsDelegateImpl
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordBaseViewModel
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordConfig
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordEditorMode
import com.example.util.simpletimetracker.feature_change_running_record.R
import com.example.util.simpletimetracker.feature_change_running_record.interactor.ChangeRunningRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_running_record.mapper.ChangeRunningRecordMapper
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.feature_comment_selection.api.CommentSelectionViewModelDelegate
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
import kotlin.Boolean

@HiltViewModel
class ChangeRunningRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    recordTagViewDataInteractor: RecordTagViewDataInteractor,
    prefsInteractor: PrefsInteractor,
    changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    changeRecordActionsDelegate: ChangeRecordActionsDelegateImpl,
    recordInteractor: RecordInteractor,
    recordTagInteractor: RecordTagInteractor,
    recordTypeToTagInteractor: RecordTypeToTagInteractor,
    snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    needTagValueSelectionInteractor: NeedTagValueSelectionInteractor,
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
    private val commentSelectionViewModelDelegate: CommentSelectionViewModelDelegate,
) : ChangeRecordBaseViewModel(
    router = router,
    resourceRepo = resourceRepo,
    snackBarMessageNavigationInteractor = snackBarMessageNavigationInteractor,
    prefsInteractor = prefsInteractor,
    recordTypesViewDataInteractor = recordTypesViewDataInteractor,
    recordTagViewDataInteractor = recordTagViewDataInteractor,
    changeRecordViewDataInteractor = changeRecordViewDataInteractor,
    recordInteractor = recordInteractor,
    recordTagInteractor = recordTagInteractor,
    recordTypeToTagInteractor = recordTypeToTagInteractor,
    changeRecordActionsDelegate = changeRecordActionsDelegate,
    needTagValueSelectionInteractor = needTagValueSelectionInteractor,
    commentSelectionViewModelDelegate = commentSelectionViewModelDelegate,
) {

    private val extra: ChangeRunningRecordParams = savedStateHandle[ARGS_PARAMS]
        ?: ChangeRunningRecordParams.Empty

    override val mode: ChangeRecordEditorMode = ChangeRecordEditorMode(
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
        afterVisible = ::afterVisible,
        afterHidden = ::afterHidden,
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

    private fun onDeleteClickMode() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            removeRunningRecordMediator.remove(extra.id)
            showMessage(R.string.change_running_record_removed)
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
            itemId = recordState.newTypeId,
            itemName = preview.name,
            itemIcon = preview.iconId,
            itemColor = preview.color,
        )
    }

    private suspend fun onSaveClickDelegate(
        doAfter: suspend () -> Unit,
    ) {
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
            comment = commentSelectionViewModelDelegate.newComment,
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

    private fun afterVisible() {
        startUpdate()
    }

    private fun afterHidden() {
        stopUpdate()
    }

    fun onMessageShown() {
        message.set(null)
    }

    override suspend fun onTimeEndedChanged() = Unit

    override suspend fun onTimeStartedChanged() {
        if (recordState.newTimeStarted > System.currentTimeMillis()) {
            recordState.newTimeStarted = System.currentTimeMillis()

            SnackBarParams(
                message = resourceRepo.getString(R.string.cannot_be_in_the_future),
                duration = SnackBarParams.Duration.Short,
            ).let(message::set)
        }
        afterTimeStartedChanged()
    }

    private suspend fun updatePreview() {
        record.set(loadPreviewViewData())
    }

    private suspend fun initializePreviewViewData() {
        if (extra.id != 0L) {
            runningRecordInteractor.get(extra.id)?.let { record ->
                recordState.newTypeId = record.id.orZero()
                recordState.newTimeStarted = record.timeStarted
                recordState.newTimeEnded = System.currentTimeMillis()
                recordState.newTags = record.tags.toMutableList()
                commentSelectionViewModelDelegate.newComment = record.comment
            }
            afterInitializePreviewViewData()
        }
    }

    private suspend fun loadPreviewViewData(): ChangeRunningRecordViewData {
        if (recordState.newTypeId == 0L) initializePreviewViewData()

        // TODO BASE move to extension
        val record = RunningRecord(
            id = recordState.newTypeId,
            timeStarted = recordState.newTimeStarted,
            comment = commentSelectionViewModelDelegate.newComment,
            tags = recordState.newTags,
        )

        return changeRunningRecordViewDataInteractor.getPreviewViewData(
            record = record,
            params = extra,
            dateTimeFieldState = dateTimeState,
        )
    }

    private fun startUpdate() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                updatePreview()
                // Update split preview only if it is visible
                if (chooserState.value?.current is ChangeRecordChooserState.State.Action) {
                    updateActionsData()
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