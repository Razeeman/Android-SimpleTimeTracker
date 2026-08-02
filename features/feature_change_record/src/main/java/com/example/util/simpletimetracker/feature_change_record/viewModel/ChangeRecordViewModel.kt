package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor.GetParam
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.AddTagToTypeIfNotExistMediator
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_change_record.api.ChangeRecordConfig
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.feature_change_record.api.ChangeRecordEditorDelegate
import com.example.util.simpletimetracker.feature_change_record.api.ChangeRecordEditorMode
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordEditorState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val editorDelegate: ChangeRecordEditorDelegate,
    private val prefsInteractor: PrefsInteractor,
    private val router: Router,
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
    private val timeMapper: TimeMapper,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val addTagToTypeIfNotExistMediator: AddTagToTypeIfNotExistMediator,
) : BaseViewModel() {

    private val extra: ChangeRecordParams = savedStateHandle[ARGS_PARAMS]
        ?: ChangeRecordParams.New(0)
    private val recordId: Long? = (extra as? ChangeRecordParams.Tracked)?.id

    private val mode: ChangeRecordEditorMode = ChangeRecordEditorMode(
        config = ChangeRecordConfig(
            forceSecondsInDurationDialog = false,
            showTimeEndedOnSplitPreview = true,
            showTimeEndedOnAdjustPreview = true,
            adjustNextRecordAvailable = true,
            isTimeEndedAvailable = true,
            isAdditionalActionsAvailable = true,
            isDuplicateActionAvailable = true,
            isDeleteButtonVisible = recordId.orZero() != 0L,
            isStatisticsButtonVisible = extra is ChangeRecordParams.Tracked || extra is ChangeRecordParams.Untracked,
        ),
        mergeAvailable = { extra is ChangeRecordParams.Untracked && editorDelegate.recordState.newTypeId == 0L },
        previewTimeEnded = { editorDelegate.recordState.newTimeEnded },
        adjustPreviewTimeEnded = { editorDelegate.recordState.newTimeEnded },
        adjustPreviewOriginalTimeEnded = { editorDelegate.recordState.originalTimeEnded },
        updatePreview = ::updatePreview,
        getChangeCategoryParams = ::getChangeCategoryParams,
        onSaveClickDelegate = ::onSaveClickDelegate,
        sendPreviewUpdate = {},
        initializePreviewViewData = ::initializePreviewViewData,
        onDeleteClick = ::onDeleteClickMode,
        onStatisticsClick = ::onStatisticsClickMode,
        onTimeStartedChanged = ::onTimeStartedChanged,
        onTimeEndedChanged = ::onTimeEndedChanged,
    )

    val record: LiveData<ChangeRecordViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordViewData>().let { initial ->
            viewModelScope.launch {
                initializePreviewViewData()
                initial.value = loadPreviewViewData()
            }
            initial
        }
    }
    val removeRecordId: LiveData<Long> = SingleLiveEvent()

    init {
        editorDelegate.attach(mode)
    }

    override fun onCleared() {
        editorDelegate.clear()
        super.onCleared()
    }

    fun onVisible() {
        editorDelegate.onVisible()
    }

    private fun onDeleteClickMode() {
        recordId?.let { removeRecordId.set(it) }
        router.back()
    }

    private fun onStatisticsClickMode() = viewModelScope.launch {
        val itemId = when {
            editorDelegate.recordState.newTypeId != 0L -> editorDelegate.recordState.newTypeId
            extra is ChangeRecordParams.Untracked -> UNTRACKED_ITEM_ID
            else -> return@launch
        }
        val preview = record.value ?: return@launch

        statisticsDetailNavigationInteractor.navigate(
            transitionName = "",
            filterType = ChartFilterType.ACTIVITY,
            shift = 0,
            overrideStatisticsRange = null,
            sharedElements = emptyMap(),
            itemId = itemId,
            itemName = preview.recordPreview.name,
            itemIcon = preview.recordPreview.iconId,
            itemColor = preview.recordPreview.color,
        )
    }

    private suspend fun onSaveClickDelegate(
        doAfter: suspend () -> Unit,
    ) {
        val recordState = editorDelegate.recordState
        // Zero id creates new record
        val id = recordId.orZero()
        mapRecordModel(
            comment = editorDelegate.commentSelectionViewModelDelegate.newComment,
            recordState = recordState,
        ).copy(id = id).let {
            addRecordMediator.add(it)
        }
        addTagToTypeIfNotExistMediator.execute(
            typeId = recordState.newTypeId,
            tagIds = recordState.newTags.map(RecordBase.Tag::tagId),
        )
        if (recordState.newTypeId != recordState.originalTypeId) {
            externalViewsInteractor.onRecordChangeType(listOf(recordState.originalTypeId))
        }
        val newTagIds = recordState.newTags.map(RecordBase.Tag::tagId)
        val removedTagIds = recordState.originalTags.map { it.tagId }.filter { it !in newTagIds }
        if (removedTagIds.isNotEmpty()) {
            externalViewsInteractor.onRecordChangeTags(removedTagIds)
        }
        doAfter()
        warmupCache(extra.daysFromToday)
        router.back()
    }

    private fun getChangeCategoryParams(data: ChangeTagData): ChangeRecordTagFromScreen {
        return ChangeRecordTagFromChangeRecordParams(data)
    }

    private suspend fun onTimeEndedChanged() {
        val recordState = editorDelegate.recordState
        if (recordState.newTimeEnded < recordState.newTimeStarted) {
            recordState.newTimeStarted = recordState.newTimeEnded
        }
        editorDelegate.afterTimeEndedChanged()
    }

    private suspend fun onTimeStartedChanged() {
        val recordState = editorDelegate.recordState
        if (recordState.newTimeStarted > recordState.newTimeEnded) {
            recordState.newTimeEnded = recordState.newTimeStarted
        }
        editorDelegate.afterTimeStartedChanged()
    }

    private suspend fun warmupCache(actualShift: Int) {
        if (prefsInteractor.getShowRecordsCalendar()) return
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = RangeLength.Day,
            shift = actualShift,
            firstDayOfWeek = DayOfWeek.MONDAY, // Doesn't matter for days.
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
        )
        recordInteractor.getWithParams(GetParam.FromRange(range))
    }

    private fun getInitialTimeEnded(daysFromToday: Int): Long {
        return timeMapper.toTimestampShifted(daysFromToday, RangeLength.Day)
    }

    private suspend fun getInitialTimeStarted(
        newTimeEnded: Long,
        daysFromToday: Int,
    ): Long {
        val default = newTimeEnded - ONE_HOUR

        return if (daysFromToday == 0) {
            recordInteractor.getPrev(newTimeEnded)?.timeEnded ?: default
        } else {
            default
        }
    }

    private fun mapRecordModel(
        comment: String,
        recordState: ChangeRecordEditorState,
    ): Record {
        return Record(
            typeId = recordState.newTypeId,
            timeStarted = recordState.newTimeStarted,
            timeEnded = recordState.newTimeEnded,
            comment = comment,
            tags = recordState.newTags,
        )
    }

    private suspend fun updatePreview() {
        record.set(loadPreviewViewData())
    }

    private suspend fun initializePreviewViewData() {
        val recordState = editorDelegate.recordState
        when (extra) {
            is ChangeRecordParams.Tracked -> {
                recordInteractor.get(recordId.orZero())?.let { record ->
                    recordState.newTypeId = record.typeId.orZero()
                    recordState.newTimeStarted = record.timeStarted
                    recordState.newTimeEnded = record.timeEnded
                    recordState.newTags = record.tags
                    editorDelegate.commentSelectionViewModelDelegate.newComment = record.comment
                }
            }
            is ChangeRecordParams.Untracked -> {
                recordState.newTimeStarted = extra.timeStarted
                recordState.newTimeEnded = extra.timeEnded
            }
            is ChangeRecordParams.New -> {
                val daysFromToday = extra.daysFromToday
                recordState.newTimeEnded = getInitialTimeEnded(daysFromToday)
                recordState.newTimeStarted = getInitialTimeStarted(recordState.newTimeEnded, daysFromToday)
            }
        }
        recordState.originalRecordId = recordId.orZero()
        editorDelegate.afterInitializePreviewViewData()
    }

    private suspend fun loadPreviewViewData(): ChangeRecordViewData {
        val record = mapRecordModel(
            comment = editorDelegate.commentSelectionViewModelDelegate.newComment,
            recordState = editorDelegate.recordState,
        )

        return changeRecordViewDataInteractor.getPreviewViewData(
            record = record,
            dateTimeFieldState = editorDelegate.dateTimeState,
        )
    }

    companion object {
        private const val ONE_HOUR: Int = 60 * 60 * 1000
    }
}
