package com.example.util.simpletimetracker.feature_change_running_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_running_record.R
import com.example.util.simpletimetracker.feature_change_running_record.interactor.ChangeRunningRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChangeRunningRecordViewModel @Inject constructor(
    private val router: Router,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val changeRunningRecordViewDataInteractor: ChangeRunningRecordViewDataInteractor,
    private val recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
) : ViewModel() {

    lateinit var extra: ChangeRunningRecordParams

    val record: LiveData<ChangeRunningRecordViewData> by lazy {
        return@lazy MutableLiveData<ChangeRunningRecordViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadPreviewViewData()
            }
            initial
        }
    }
    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadTypesViewData() }
            initial
        }
    }
    val categories: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializePreviewViewData()
                initial.value = loadCategoriesViewData()
            }
            initial
        }
    }
    val timeAdjustmentItems: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(loadTimeAdjustmentItems())
    }
    val timeAdjustmentState: LiveData<Boolean> = MutableLiveData(false)
    val message: LiveData<SnackBarParams?> = MutableLiveData()
    val flipTypesChooser: LiveData<Boolean> = MutableLiveData()
    val flipCategoryChooser: LiveData<Boolean> = MutableLiveData()
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)

    private var newTypeId: Long = 0
    private var newTimeStarted: Long = 0
    private var timerJob: Job? = null
    private var newComment: String = ""
    private var newCategoryIds: MutableList<Long> = mutableListOf()

    fun onTypeChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipTypesChooser as MutableLiveData).value = flipTypesChooser.value
            ?.flip().orTrue()

        if (flipCategoryChooser.value == true) {
            (flipCategoryChooser as MutableLiveData).value = false
        }
    }

    fun onCategoryChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipCategoryChooser as MutableLiveData).value = flipCategoryChooser.value
            ?.flip().orTrue()

        if (flipTypesChooser.value == true) {
            (flipTypesChooser as MutableLiveData).value = false
        }
    }

    fun onTimeStartedClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

            router.navigate(
                DateTimeDialogParams(
                    tag = TIME_STARTED_TAG,
                    timestamp = newTimeStarted,
                    type = DateTimeDialogType.DATETIME(),
                    useMilitaryTime = useMilitaryTime,
                    firstDayOfWeek = firstDayOfWeek
                )
            )
        }
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            removeRunningRecordMediator.remove(extra.id)
            showMessage(R.string.change_running_record_removed)
            (keyboardVisibility as MutableLiveData).value = false
            router.back()
        }
    }

    fun onSaveClick() {
        if (newTypeId == 0L) {
            showMessage(R.string.change_record_message_choose_type)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            removeRunningRecordMediator.remove(extra.id)
            addRunningRecordMediator.add(newTypeId, newTimeStarted, newComment, newCategoryIds)
            (keyboardVisibility as MutableLiveData).value = false
            router.back()
        }
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (item.id != newTypeId) {
                newTypeId = item.id
                newCategoryIds.clear()
                updatePreview()
                updateCategoriesViewData()
            }
        }
    }

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            when (item) {
                is CategoryViewData.Record.Tagged -> {
                    newCategoryIds.addOrRemove(item.id)
                }
                is CategoryViewData.Record.Untagged -> {
                    newCategoryIds.clear()
                }
                else -> return@launch
            }
            updatePreview()
            updateCategoriesViewData()
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModelScope.launch {
            when (tag) {
                TIME_STARTED_TAG -> {
                    if (timestamp != newTimeStarted) {
                        newTimeStarted = timestamp
                        checkTimeStarted()
                        updatePreview()
                    }
                }
            }
        }
    }

    fun onCommentChange(comment: String) {
        viewModelScope.launch {
            if (comment != newComment) {
                newComment = comment
                updatePreview()
            }
        }
    }

    fun onAdjustTimeStartedClick() {
        val newValue = timeAdjustmentState.value.orFalse().flip()
        timeAdjustmentState.set(newValue)
    }

    fun onAdjustTimeItemClick(viewData: TimeAdjustmentView.ViewData) {
        viewModelScope.launch {
            when (viewData) {
                is TimeAdjustmentView.ViewData.Now -> {
                    newTimeStarted = System.currentTimeMillis()
                    updatePreview()
                }
                is TimeAdjustmentView.ViewData.Adjust -> {
                    newTimeStarted += TimeUnit.MINUTES.toMillis(viewData.value)
                    checkTimeStarted()
                    updatePreview()
                }
            }
        }
    }

    fun onVisible() {
        startUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    fun onMessageShown() {
        message.set(null)
    }

    private fun checkTimeStarted() {
        if (newTimeStarted > System.currentTimeMillis()) {
            newTimeStarted = System.currentTimeMillis()

            SnackBarParams(
                message = resourceRepo.getString(R.string.cannot_be_in_the_future),
                isShortDuration = true
            ).let(message::set)
        }
    }

    private suspend fun updatePreview() {
        (record as MutableLiveData).value = loadPreviewViewData()
    }

    private suspend fun initializePreviewViewData() {
        if (extra.id != 0L) {
            runningRecordInteractor.get(extra.id)?.let { record ->
                newTypeId = record.id.orZero()
                newTimeStarted = record.timeStarted
                newComment = record.comment
                newCategoryIds = record.tagIds.toMutableList()
            }
        }
    }

    private suspend fun loadPreviewViewData(): ChangeRunningRecordViewData {
        if (newTypeId == 0L) initializePreviewViewData()

        val record = RunningRecord(
            id = newTypeId,
            timeStarted = newTimeStarted,
            comment = newComment,
            tagIds = newCategoryIds
        )

        return changeRunningRecordViewDataInteractor.getPreviewViewData(record)
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return recordTypesViewDataInteractor.getTypesViewData()
    }

    private fun updateCategoriesViewData() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        categories.set(data)
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return recordTagViewDataInteractor.getViewData(
            selectedTags = newCategoryIds,
            typeId = newTypeId,
            multipleChoiceAvailable = true,
        )
    }

    private fun loadTimeAdjustmentItems(): List<ViewHolderType> {
        return changeRunningRecordViewDataInteractor.getTimeAdjustmentItems()
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updatePreview()
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
        }
    }

    private fun showMessage(stringResId: Int) {
        val params = ToastParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }

    companion object {
        private const val TIMER_UPDATE = 1000L
        private const val TIME_STARTED_TAG = "time_started_tag"
    }
}