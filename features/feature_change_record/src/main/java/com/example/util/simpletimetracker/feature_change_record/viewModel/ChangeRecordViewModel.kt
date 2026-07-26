package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor.GetParam
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.AddTagToTypeIfNotExistMediator
import com.example.util.simpletimetracker.domain.recordTag.interactor.NeedTagValueSelectionInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.feature_comment_selection.api.CommentSelectionViewModelDelegate
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Boolean

@HiltViewModel
class ChangeRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    resourceRepo: ResourceRepo,
    recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    recordTagViewDataInteractor: RecordTagViewDataInteractor,
    snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    changeRecordActionsDelegate: ChangeRecordActionsDelegateImpl,
    recordTagInteractor: RecordTagInteractor,
    recordTypeToTagInteractor: RecordTypeToTagInteractor,
    needTagValueSelectionInteractor: NeedTagValueSelectionInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val router: Router,
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
    private val timeMapper: TimeMapper,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
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

    private val extra: ChangeRecordParams = savedStateHandle[ARGS_PARAMS]
        ?: ChangeRecordParams.New(0)
    private val recordId: Long? = (extra as? ChangeRecordParams.Tracked)?.id

    override val config: ChangeRecordConfig = ChangeRecordConfig(
        forceSecondsInDurationDialog = false,
        showTimeEndedOnSplitPreview = true,
        showTimeEndedOnAdjustPreview = true,
        adjustNextRecordAvailable = true,
        isTimeEndedAvailable = true,
        isAdditionalActionsAvailable = true,
        isDuplicateActionAvailable = true,
        isDeleteButtonVisible = recordId.orZero() != 0L,
        isStatisticsButtonVisible = extra is ChangeRecordParams.Tracked || extra is ChangeRecordParams.Untracked,
    )
    override val mergeAvailable: Boolean get() = extra is ChangeRecordParams.Untracked && newTypeId == 0L
    override val previewTimeEnded: Long get() = newTimeEnded
    override val adjustPreviewTimeEnded: Long get() = newTimeEnded
    override val adjustPreviewOriginalTimeEnded: Long get() = originalTimeEnded

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

    fun onVisible() {
        viewModelScope.launch {
            updateCategoriesViewData()
        }
    }

    fun onDeleteClick() {
        recordId?.let { removeRecordId.set(it) }
        router.back()
    }

    fun onStatisticsClick() = viewModelScope.launch {
        val itemId = when {
            newTypeId != 0L -> newTypeId
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

    override suspend fun onSaveClickDelegate(
        doAfter: suspend () -> Unit,
    ) {
        // Zero id creates new record
        val id = recordId.orZero()
        Record(
            id = id,
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded,
            comment = commentSelectionViewModelDelegate.newComment,
            tags = newTags,
        ).let {
            addRecordMediator.add(it)
        }
        addTagToTypeIfNotExistMediator.execute(
            typeId = newTypeId,
            tagIds = newTags.map(RecordBase.Tag::tagId),
        )
        if (newTypeId != originalTypeId) {
            externalViewsInteractor.onRecordChangeType(listOf(originalTypeId))
        }
        val newTagIds = newTags.map(RecordBase.Tag::tagId)
        val removedTagIds = originalTags.map { it.tagId }.filter { it !in newTagIds }
        if (removedTagIds.isNotEmpty()) {
            externalViewsInteractor.onRecordChangeTags(removedTagIds)
        }
        doAfter()
        warmupCache(extra.daysFromToday)
        router.back()
    }

    override fun getChangeCategoryParams(data: ChangeTagData): ChangeRecordTagFromScreen {
        return ChangeRecordTagFromChangeRecordParams(data)
    }

    override suspend fun onTimeEndedChanged() {
        if (newTimeEnded < newTimeStarted) newTimeStarted = newTimeEnded
        if (newTimeEnded < newTimeSplit) newTimeSplit = newTimeEnded
        super.onTimeEndedChanged()
    }

    override suspend fun onTimeStartedChanged() {
        if (newTimeStarted > newTimeEnded) newTimeEnded = newTimeStarted
        if (newTimeStarted > newTimeSplit) newTimeSplit = newTimeStarted
        super.onTimeStartedChanged()
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

    private suspend fun getInitialTimeStarted(daysFromToday: Int): Long {
        val default = newTimeEnded - ONE_HOUR

        return if (daysFromToday == 0) {
            recordInteractor.getPrev(newTimeEnded)?.timeEnded ?: default
        } else {
            default
        }
    }

    override suspend fun updatePreview() {
        record.set(loadPreviewViewData())
    }

    override suspend fun initializePreviewViewData() {
        when (extra) {
            is ChangeRecordParams.Tracked -> {
                recordInteractor.get(recordId.orZero())?.let { record ->
                    newTypeId = record.typeId.orZero()
                    newTimeStarted = record.timeStarted
                    newTimeEnded = record.timeEnded
                    commentSelectionViewModelDelegate.newComment = record.comment
                    newTags = record.tags
                }
            }
            is ChangeRecordParams.Untracked -> {
                newTimeStarted = extra.timeStarted
                newTimeEnded = extra.timeEnded
            }
            is ChangeRecordParams.New -> {
                val daysFromToday = extra.daysFromToday
                newTimeEnded = getInitialTimeEnded(daysFromToday)
                newTimeStarted = getInitialTimeStarted(daysFromToday)
            }
        }
        newTimeSplit = newTimeStarted
        originalRecordId = recordId.orZero()
        originalTypeId = newTypeId
        originalTags = newTags.toList() // Creates a copy.
        originalTimeStarted = newTimeStarted
        originalTimeEnded = newTimeEnded
        super.initializePreviewViewData()
    }

    private suspend fun loadPreviewViewData(): ChangeRecordViewData {
        val record = Record(
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded,
            comment = commentSelectionViewModelDelegate.newComment,
            tags = newTags,
        )

        return changeRecordViewDataInteractor.getPreviewViewData(
            record = record,
            dateTimeFieldState = dateTimeState,
        )
    }

    companion object {
        private const val ONE_HOUR: Int = 60 * 60 * 1000
    }
}
