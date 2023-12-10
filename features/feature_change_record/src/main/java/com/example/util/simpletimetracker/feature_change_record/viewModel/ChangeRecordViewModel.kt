package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
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
    resourceRepo: ResourceRepo,
    changeRecordMergeDelegate: ChangeRecordMergeDelegateImpl,
    changeRecordSplitDelegate: ChangeRecordSplitDelegateImpl,
    changeRecordAdjustDelegate: ChangeRecordAdjustDelegateImpl,
    recordTagInteractor: RecordTagInteractor,
    favouriteCommentInteractor: FavouriteCommentInteractor,
    private val router: Router,
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val removeRecordMediator: RemoveRecordMediator,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val timeMapper: TimeMapper,
) : ChangeRecordBaseViewModel(
    router,
    resourceRepo,
    prefsInteractor,
    recordTypesViewDataInteractor,
    recordTagViewDataInteractor,
    changeRecordViewDataInteractor,
    recordInteractor,
    recordTagInteractor,
    favouriteCommentInteractor,
    changeRecordMergeDelegate,
    changeRecordSplitDelegate,
    changeRecordAdjustDelegate,
) {

    lateinit var extra: ChangeRecordParams

    override val mergeAvailable: Boolean get() = extra is ChangeRecordParams.Untracked && newTypeId == 0L
    override val splitPreviewTimeEnded: Long get() = newTimeEnded
    override val showTimeEndedOnSplitPreview: Boolean get() = true
    override val adjustNextRecordAvailable: Boolean get() = true

    val record: LiveData<ChangeRecordViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordViewData>().let { initial ->
            viewModelScope.launch {
                initializePreviewViewData()
                initial.value = loadPreviewViewData()
            }
            initial
        }
    }

    fun onVisible() {
        viewModelScope.launch {
            updateCategoriesViewData()
        }
    }

    fun onDeleteClick() {
        router.back()
    }

    override suspend fun onSaveClickDelegate() {
        // Zero id creates new record
        val id = (extra as? ChangeRecordParams.Tracked)?.id.orZero()
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

    override suspend fun onContinueClickDelegate() {
        // Remove current record if exist.
        (extra as? ChangeRecordParams.Tracked)?.id?.let {
            val typeId = recordInteractor.get(it)?.typeId.orZero()
            removeRecordMediator.remove(it, typeId)
        }
        // Stop same type running record if exist (only one of the same type can run at once).
        runningRecordInteractor.get(newTypeId)
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
        // Add new running record.
        addRunningRecordMediator.startTimer(
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            comment = newComment,
            tagIds = newCategoryIds,
        )
        // Exit.
        router.back()
    }

    override suspend fun onDuplicateClickDelegate() {
        Record(
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded,
            comment = newComment,
            tagIds = newCategoryIds,
        ).let {
            addRecordMediator.add(it)
        }
        onSaveClickDelegate()
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
            recordInteractor.getPrev(newTimeEnded, limit = 1)
                .firstOrNull()
                ?.timeEnded
                ?: default
        } else {
            default
        }
    }

    override suspend fun updatePreview() {
        (record as MutableLiveData).value = loadPreviewViewData()
    }

    override suspend fun initializePreviewViewData() {
        when (extra) {
            is ChangeRecordParams.Tracked -> {
                recordInteractor.get((extra as ChangeRecordParams.Tracked).id)?.let { record ->
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

        return changeRecordViewDataInteractor.getPreviewViewData(record)
    }

    companion object {
        private const val ONE_HOUR: Int = 60 * 60 * 1000
    }
}
