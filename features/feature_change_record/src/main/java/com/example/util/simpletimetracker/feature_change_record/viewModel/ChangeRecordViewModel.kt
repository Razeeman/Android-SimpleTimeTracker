package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordCommentViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.TimeAdjustmentState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChangeRecordViewModel @Inject constructor(
    private val router: Router,
    private val recordInteractor: RecordInteractor,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val widgetInteractor: WidgetInteractor,
) : ViewModel() {

    lateinit var extra: ChangeRecordParams

    val record: LiveData<ChangeRecordViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordViewData>().let { initial ->
            viewModelScope.launch {
                initializePreviewViewData()
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
    val lastComments: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializePreviewViewData()
                initial.value = loadLastCommentsViewData()
            }
            initial
        }
    }
    val timeAdjustmentItems: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(loadTimeAdjustmentItems())
    }
    val timeAdjustmentState: LiveData<TimeAdjustmentState> = MutableLiveData(TimeAdjustmentState.HIDDEN)
    val flipTypesChooser: LiveData<Boolean> = MutableLiveData()
    val flipCategoryChooser: LiveData<Boolean> = MutableLiveData()
    val flipLastCommentsChooser: LiveData<Boolean> = MutableLiveData()
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)
    val comment: LiveData<String> = MutableLiveData()

    private var newTypeId: Long = 0
    private var newTimeEnded: Long = 0
    private var newTimeStarted: Long = 0
    private var newComment: String = ""
    private var newCategoryIds: MutableList<Long> = mutableListOf()

    fun onVisible() {
        updateCategoriesViewData()
    }

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

    fun onTimeEndedClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

            router.navigate(
                DateTimeDialogParams(
                    tag = TIME_ENDED_TAG,
                    timestamp = newTimeEnded,
                    type = DateTimeDialogType.DATETIME(),
                    useMilitaryTime = useMilitaryTime,
                    firstDayOfWeek = firstDayOfWeek
                )
            )
        }
    }

    fun onLastCommentsChooserClick() {
        (flipLastCommentsChooser as MutableLiveData).value = flipLastCommentsChooser.value
            ?.flip().orTrue()
    }

    fun onDeleteClick() {
        (keyboardVisibility as MutableLiveData).value = false
        router.back()
    }

    fun onSaveClick() {
        if (newTypeId == 0L) {
            showMessage(R.string.change_record_message_choose_type)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            // Zero id creates new record
            val id = (extra as? ChangeRecordParams.Tracked)?.id.orZero()
            Record(
                id = id,
                typeId = newTypeId,
                timeStarted = newTimeStarted,
                timeEnded = newTimeEnded,
                comment = newComment,
                tagIds = newCategoryIds
            ).let {
                recordInteractor.add(it)
                widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (item.id != newTypeId) {
                newTypeId = item.id
                newCategoryIds.clear()
                updatePreview()
                updateCategoriesViewData()
                updateLastCommentsViewData()
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

    fun onCategoryLongClick(item: CategoryViewData, sharedElements: Pair<Any, String>) {
        val icon = (item as? CategoryViewData.Record)?.icon?.toParams()

        router.navigate(
            data = ChangeRecordTagFromChangeRecordParams(
                ChangeTagData.Change(
                    transitionName = sharedElements.second,
                    id = item.id,
                    preview = ChangeTagData.Change.Preview(
                        name = item.name,
                        color = item.color,
                        icon = icon,
                    )
                )
            ),
            sharedElements = mapOf(sharedElements)
        )
    }

    fun onAddCategoryClick() {
        val preselectedTypeId: Long? = newTypeId.takeUnless { it == 0L }
        router.navigate(
            data = ChangeRecordTagFromChangeRecordParams(
                ChangeTagData.New(preselectedTypeId)
            )
        )
    }

    fun onCommentClick(item: ChangeRecordCommentViewData) {
        comment.set(item.text)
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModelScope.launch {
            when (tag) {
                TIME_STARTED_TAG -> {
                    if (timestamp != newTimeStarted) {
                        newTimeStarted = timestamp
                        checkTimeEnded()
                        updatePreview()
                    }
                }
                TIME_ENDED_TAG -> {
                    if (timestamp != newTimeEnded) {
                        newTimeEnded = timestamp
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
        updateAdjustTimeState(
            clicked = TimeAdjustmentState.TIME_STARTED,
            other = TimeAdjustmentState.TIME_ENDED
        )
    }

    fun onAdjustTimeEndedClick() {
        updateAdjustTimeState(
            clicked = TimeAdjustmentState.TIME_ENDED,
            other = TimeAdjustmentState.TIME_STARTED
        )
    }

    fun onAdjustTimeItemClick(viewData: TimeAdjustmentView.ViewData) {
        when (viewData) {
            is TimeAdjustmentView.ViewData.Now -> onAdjustTimeNowClick()
            is TimeAdjustmentView.ViewData.Adjust -> adjustRecordTime(viewData.value)
        }
    }

    private fun updateAdjustTimeState(
        clicked: TimeAdjustmentState,
        other: TimeAdjustmentState,
    ) {
        when (timeAdjustmentState.value) {
            TimeAdjustmentState.HIDDEN -> {
                timeAdjustmentState.set(clicked)
            }
            clicked -> {
                timeAdjustmentState.set(TimeAdjustmentState.HIDDEN)
            }
            other -> viewModelScope.launch {
                timeAdjustmentState.set(TimeAdjustmentState.HIDDEN)
                delay(300)
                timeAdjustmentState.set(clicked)
            }
            else -> {
                // Do nothing
            }
        }
    }

    private fun onAdjustTimeNowClick() = viewModelScope.launch {
        when (timeAdjustmentState.value) {
            TimeAdjustmentState.TIME_STARTED -> {
                newTimeStarted = System.currentTimeMillis()
                checkTimeEnded()
            }
            TimeAdjustmentState.TIME_ENDED -> {
                newTimeEnded = System.currentTimeMillis()
                checkTimeStarted()
            }
            else -> {
                // Do nothing, it's hidden.
            }
        }
        updatePreview()
    }

    private fun adjustRecordTime(shiftInMinutes: Long) = viewModelScope.launch {
        when (timeAdjustmentState.value) {
            TimeAdjustmentState.TIME_STARTED -> {
                newTimeStarted += TimeUnit.MINUTES.toMillis(shiftInMinutes)
                checkTimeEnded()
            }
            TimeAdjustmentState.TIME_ENDED -> {
                newTimeEnded += TimeUnit.MINUTES.toMillis(shiftInMinutes)
                checkTimeStarted()
            }
            else -> {
                // Do nothing, it's hidden.
            }
        }
        updatePreview()
    }

    private fun checkTimeStarted() {
        if (newTimeEnded < newTimeStarted) newTimeStarted = newTimeEnded
    }

    private fun checkTimeEnded() {
        if (newTimeStarted > newTimeEnded) newTimeEnded = newTimeStarted
    }

    private fun getInitialDate(daysFromToday: Int): Long {
        return timeMapper.toTimestampShifted(daysFromToday, RangeLength.Day)
    }

    private suspend fun updatePreview() {
        (record as MutableLiveData).value = loadPreviewViewData()
    }

    private suspend fun initializePreviewViewData() {
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
                newTimeEnded = getInitialDate((extra as ChangeRecordParams.New).daysFromToday)
                newTimeStarted = newTimeEnded - ONE_HOUR
            }
        }
    }

    private suspend fun loadPreviewViewData(): ChangeRecordViewData {
        val record = Record(
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded,
            comment = newComment,
            tagIds = newCategoryIds
        )

        return changeRecordViewDataInteractor.getPreviewViewData(record)
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
            showHint = true,
            showAddButton = true,
        )
    }

    private fun updateLastCommentsViewData() = viewModelScope.launch {
        val data = loadLastCommentsViewData()
        lastComments.set(data)
        if (data.isEmpty()) flipLastCommentsChooser.set(false)
    }

    private suspend fun loadLastCommentsViewData(): List<ViewHolderType> {
        return changeRecordViewDataInteractor.getLastCommentsViewData(newTypeId)
    }

    private fun loadTimeAdjustmentItems(): List<ViewHolderType> {
        return changeRecordViewDataInteractor.getTimeAdjustmentItems()
    }

    private fun showMessage(stringResId: Int) {
        val params = ToastParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }

    companion object {
        private const val TIME_STARTED_TAG = "time_started_tag"
        private const val TIME_ENDED_TAG = "time_ended_tag"

        private const val ONE_HOUR: Int = 60 * 60 * 1000
    }
}
