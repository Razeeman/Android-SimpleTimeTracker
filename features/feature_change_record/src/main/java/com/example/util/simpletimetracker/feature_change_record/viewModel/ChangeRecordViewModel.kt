package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeRecordViewModel @Inject constructor(
    recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    recordTagViewDataInteractor: RecordTagViewDataInteractor,
    prefsInteractor: PrefsInteractor,
    snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    changeRecordActionsDelegate: ChangeRecordActionsDelegateImpl,
    recordTypeToTagInteractor: RecordTypeToTagInteractor,
    favouriteCommentInteractor: FavouriteCommentInteractor,
    private val router: Router,
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val timeMapper: TimeMapper,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
) : ChangeRecordBaseViewModel(
    router = router,
    snackBarMessageNavigationInteractor = snackBarMessageNavigationInteractor,
    prefsInteractor = prefsInteractor,
    recordTypesViewDataInteractor = recordTypesViewDataInteractor,
    recordTagViewDataInteractor = recordTagViewDataInteractor,
    changeRecordViewDataInteractor = changeRecordViewDataInteractor,
    recordInteractor = recordInteractor,
    recordTypeToTagInteractor = recordTypeToTagInteractor,
    favouriteCommentInteractor = favouriteCommentInteractor,
    changeRecordActionsDelegate = changeRecordActionsDelegate,
) {

    lateinit var extra: ChangeRecordParams

    override val forceSecondsInDurationDialog: Boolean get() = false
    override val mergeAvailable: Boolean get() = extra is ChangeRecordParams.Untracked && newTypeId == 0L
    override val previewTimeEnded: Long get() = newTimeEnded
    override val showTimeEndedOnSplitPreview: Boolean get() = true
    override val adjustPreviewTimeEnded: Long get() = newTimeEnded
    override val adjustPreviewOriginalTimeEnded: Long get() = originalTimeEnded
    override val showTimeEndedOnAdjustPreview: Boolean get() = true
    override val adjustNextRecordAvailable: Boolean get() = true
    override val isTimeEndedAvailable: Boolean get() = true
    override val isAdditionalActionsAvailable: Boolean get() = true
    override val isDeleteButtonVisible: Boolean get() = recordId.orZero() != 0L
    override val isStatisticsButtonVisible: Boolean
        get() = extra is ChangeRecordParams.Tracked ||
            extra is ChangeRecordParams.Untracked

    val record: LiveData<ChangeRecordViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordViewData>().let { initial ->
            viewModelScope.launch {
                initializePreviewViewData()
                initial.value = loadPreviewViewData()
            }
            initial
        }
    }

    private val recordId: Long? get() = (extra as? ChangeRecordParams.Tracked)?.id

    fun onVisible() {
        viewModelScope.launch {
            updateCategoriesViewData()
        }
    }

    fun onDeleteClick() {
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
            sharedElements = emptyMap(),
            itemId = itemId,
            itemName = preview.name,
            itemIcon = preview.iconId,
            itemColor = preview.color,
        )
    }

    override suspend fun onSaveClickDelegate() {
        // Zero id creates new record
        val id = recordId.orZero()
        Record(
            id = id,
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded,
            comment = newComment,
            tagIds = newCategoryIds,
        ).let {
            addRecordMediator.add(it)
            if (newTypeId != originalTypeId) {
                notificationTypeInteractor.checkAndShow(originalTypeId)
                notificationGoalTimeInteractor.checkAndReschedule(listOf(originalTypeId))
            }
            router.back()
        }
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

    private fun getInitialTimeEnded(daysFromToday: Int): Long {
        return timeMapper.toTimestampShifted(daysFromToday, RangeLength.Day)
    }

    private suspend fun getInitialTimeStarted(daysFromToday: Int): Long {
        val default = newTimeEnded - ONE_HOUR

        return if (daysFromToday == 0) {
            recordInteractor.getPrev(newTimeEnded)
                .firstOrNull()
                ?.timeEnded
                ?: default
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
                    newComment = record.comment
                    newCategoryIds = record.tagIds.toMutableList()
                }
            }
            is ChangeRecordParams.Untracked -> {
                newTimeStarted = (extra as ChangeRecordParams.Untracked).timeStarted
                newTimeEnded = (extra as ChangeRecordParams.Untracked).timeEnded
            }
            is ChangeRecordParams.New -> {
                val daysFromToday = (extra as ChangeRecordParams.New).daysFromToday
                newTimeEnded = getInitialTimeEnded(daysFromToday)
                newTimeStarted = getInitialTimeStarted(daysFromToday)
            }
        }
        newTimeSplit = newTimeStarted
        originalRecordId = recordId.orZero()
        originalTypeId = newTypeId
        originalTimeStarted = newTimeStarted
        originalTimeEnded = newTimeEnded
        super.initializePreviewViewData()
    }

    private suspend fun loadPreviewViewData(): ChangeRecordViewData {
        val record = Record(
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded,
            comment = newComment,
            tagIds = newCategoryIds,
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
