package com.example.util.simpletimetracker.feature_change_running_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordAdjustDelegateImpl
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordBaseViewModel
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordMergeDelegateImpl
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordSplitDelegateImpl
import com.example.util.simpletimetracker.feature_change_running_record.R
import com.example.util.simpletimetracker.feature_change_running_record.interactor.ChangeRunningRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeRunningRecordViewModel @Inject constructor(
    recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    recordTagViewDataInteractor: RecordTagViewDataInteractor,
    prefsInteractor: PrefsInteractor,
    changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    changeRecordMergeDelegate: ChangeRecordMergeDelegateImpl,
    changeRecordSplitDelegate: ChangeRecordSplitDelegateImpl,
    changeRecordAdjustDelegate: ChangeRecordAdjustDelegateImpl,
    recordInteractor: RecordInteractor,
    recordTagInteractor: RecordTagInteractor,
    favouriteCommentInteractor: FavouriteCommentInteractor,
    snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val router: Router,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val changeRunningRecordViewDataInteractor: ChangeRunningRecordViewDataInteractor,
    private val resourceRepo: ResourceRepo,
) : ChangeRecordBaseViewModel(
    router,
    snackBarMessageNavigationInteractor,
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

    lateinit var extra: ChangeRunningRecordParams

    override val mergeAvailable: Boolean = false
    override val splitPreviewTimeEnded: Long get() = System.currentTimeMillis()
    override val showTimeEndedOnSplitPreview: Boolean get() = false
    override val adjustNextRecordAvailable: Boolean get() = false

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

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            removeRunningRecordMediator.remove(extra.id, updateWidgets = true)
            showMessage(R.string.change_running_record_removed)
            router.back()
        }
    }

    override suspend fun onSaveClickDelegate() {
        // Widgets will update on adding.
        removeRunningRecordMediator.remove(extra.id, updateWidgets = false)
        addRunningRecordMediator.add(newTypeId, newTimeStarted, newComment, newCategoryIds)
        router.back()
    }

    override fun getChangeCategoryParams(data: ChangeTagData): ChangeRecordTagFromScreen {
        return ChangeRecordTagFromChangeRunningRecordParams(data)
    }

    fun onVisible() {
        viewModelScope.launch {
            updateCategoriesViewData()
        }
        startUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    fun onMessageShown() {
        message.set(null)
    }

    override suspend fun onTimeStartedChanged() {
        if (newTimeStarted > System.currentTimeMillis()) {
            newTimeStarted = System.currentTimeMillis()

            SnackBarParams(
                message = resourceRepo.getString(R.string.cannot_be_in_the_future),
                duration = SnackBarParams.Duration.Short,
            ).let(message::set)
        }
        if (newTimeStarted > newTimeSplit) newTimeSplit = newTimeStarted
        super.onTimeStartedChanged()
    }

    override suspend fun updatePreview() {
        (record as MutableLiveData).value = loadPreviewViewData()
    }

    override suspend fun initializePreviewViewData() {
        if (extra.id != 0L) {
            runningRecordInteractor.get(extra.id)?.let { record ->
                newTypeId = record.id.orZero()
                newTimeStarted = record.timeStarted
                newTimeEnded = System.currentTimeMillis()
                newComment = record.comment
                newCategoryIds = record.tagIds.toMutableList()
            }
            newTimeSplit = newTimeStarted
            originalTypeId = newTypeId
            originalTimeStarted = newTimeStarted
            originalTimeEnded = newTimeEnded
            super.initializePreviewViewData()
        }
    }

    private suspend fun loadPreviewViewData(): ChangeRunningRecordViewData {
        if (newTypeId == 0L) initializePreviewViewData()

        val record = RunningRecord(
            id = newTypeId,
            timeStarted = newTimeStarted,
            comment = newComment,
            tagIds = newCategoryIds,
        )

        return changeRunningRecordViewDataInteractor.getPreviewViewData(record, extra)
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updatePreview()
                // Update split preview only if it is visible
                if (chooserState.value?.current is ChangeRecordChooserState.State.Action) {
                    updateTimeSplitData()
                }
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