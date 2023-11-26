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
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.model.FavouriteComment
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.model.TimeAdjustmentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordCommentViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordFavCommentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSearchCommentState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Job

abstract class ChangeRecordBaseViewModel(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val recordInteractor: RecordInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val changeRecordMergeDelegate: ChangeRecordMergeDelegateImpl,
    private val changeRecordSplitDelegate: ChangeRecordSplitDelegateImpl,
    private val changeRecordAdjustDelegate: ChangeRecordAdjustDelegateImpl,
) : ViewModel(),
    ChangeRecordMergeDelegate by changeRecordMergeDelegate,
    ChangeRecordSplitDelegate by changeRecordSplitDelegate,
    ChangeRecordAdjustDelegate by changeRecordAdjustDelegate {

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
    val chooserState: LiveData<ChangeRecordChooserState> = MutableLiveData(
        ChangeRecordChooserState(
            current = ChangeRecordChooserState.State.Closed,
            previous = ChangeRecordChooserState.State.Closed,
        )
    )
    val searchCommentViewData: LiveData<ChangeRecordSearchCommentState> by lazy {
        return@lazy MutableLiveData<ChangeRecordSearchCommentState>().let { initial ->
            viewModelScope.launch {
                initial.value = loadSearchCommentViewData(isLoading = false, isEnabled = false)
            }
            initial
        }
    }
    val untrackedTimeHintVisibility: LiveData<Boolean> by lazy {
        return@lazy MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = loadUntrackedTimeHintVisibility()
            }
            initial
        }
    }
    val timeAdjustmentState: LiveData<TimeAdjustmentState> = MutableLiveData(TimeAdjustmentState.TIME_STARTED)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)
    val comment: LiveData<String> = MutableLiveData()
    val favCommentViewData: LiveData<ChangeRecordFavCommentState> = MutableLiveData()

    protected var newTypeId: Long = 0
    protected var newTimeEnded: Long = 0
    protected var newTimeStarted: Long = 0
    protected var newTimeSplit: Long = 0
    protected var newComment: String = ""
    protected var newCategoryIds: MutableList<Long> = mutableListOf()
    protected var originalTypeId: Long = 0
    protected var originalTimeStarted: Long = 0
    protected var originalTimeEnded: Long = 0

    protected abstract suspend fun updatePreview()
    protected abstract fun getChangeCategoryParams(data: ChangeTagData): ChangeRecordTagFromScreen
    protected abstract suspend fun onSaveClickDelegate()
    protected open suspend fun onContinueClickDelegate() {}
    protected open suspend fun onDuplicateClickDelegate() {}
    protected abstract val mergeAvailable: Boolean
    protected abstract val splitPreviewTimeEnded: Long
    protected abstract val showTimeEndedOnSplitPreview: Boolean
    protected abstract val adjustNextRecordAvailable: Boolean
    protected abstract val untrackedHintAvailable: Boolean

    private var prevRecord: Record? = null
    private var nextRecord: Record? = null
    private var searchComment: String = ""
    private var searchLoadJob: Job? = null

    protected open suspend fun initializePreviewViewData() {
        // Don't wait for the completion.
        viewModelScope.launch { initializeActions() }
        viewModelScope.launch { updateFavCommentViewData() }
    }

    protected open suspend fun onTimeStartedChanged() {
        updatePreview()
        updateTimeSplitData()
        updateAdjustData()
    }

    protected open suspend fun onTimeEndedChanged() {
        updatePreview()
        updateTimeSplitData()
        updateAdjustData()
    }

    protected suspend fun updateTimeSplitData() {
        changeRecordSplitDelegate.updateTimeSplitValue(
            newTimeSplit = newTimeSplit
        )
        changeRecordSplitDelegate.updateSplitPreviewViewData(
            newTypeId = newTypeId,
            newTimeStarted = newTimeStarted,
            newTimeSplit = newTimeSplit,
            newTimeEnded = splitPreviewTimeEnded,
            showTimeEnded = showTimeEndedOnSplitPreview,
        )
    }

    fun onTypeChooserClick() {
        onNewChooserState(ChangeRecordChooserState.State.Activity)
    }

    fun onCategoryChooserClick() {
        onNewChooserState(ChangeRecordChooserState.State.Tag)
    }

    fun onCommentChooserClick() {
        onNewChooserState(ChangeRecordChooserState.State.Comment)
    }

    fun onActionChooserClick() {
        onNewChooserState(ChangeRecordChooserState.State.Action)
    }

    fun onTimeStartedClick() {
        onTimeClick(tag = TIME_STARTED_TAG, timestamp = newTimeStarted)
    }

    fun onTimeEndedClick() {
        onTimeClick(tag = TIME_ENDED_TAG, timestamp = newTimeEnded)
    }

    fun onTimeSplitClick() {
        onTimeClick(tag = TIME_SPLIT_TAG, timestamp = newTimeSplit)
    }

    fun onSaveClick() {
        onRecordChangeButtonClick(
            onProceed = ::onSaveClickDelegate,
        )
    }

    fun onSplitClick() {
        onRecordChangeButtonClick(
            onProceed = {
                changeRecordSplitDelegate.onSplitClickDelegate(
                    newTypeId = newTypeId,
                    newTimeStarted = newTimeStarted,
                    newTimeSplit = newTimeSplit,
                    newComment = newComment,
                    newCategoryIds = newCategoryIds,
                    onSplitComplete = {
                        newTimeStarted = newTimeSplit
                        onSaveClick()
                    }
                )
            },
        )
    }

    fun onAdjustClick() {
        onRecordChangeButtonClick(
            onProceed = {
                changeRecordAdjustDelegate.onAdjustClickDelegate(
                    adjustNextRecordAvailable = adjustNextRecordAvailable,
                    prevRecord = prevRecord,
                    nextRecord = nextRecord,
                    newTimeStarted = newTimeStarted,
                    newTimeEnded = newTimeEnded,
                    onAdjustComplete = {
                        onSaveClick()
                    }
                )
            }
        )
    }

    fun onContinueClick() {
        // Can't continue future record
        if (newTimeStarted > System.currentTimeMillis()) {
            showMessage(R.string.cannot_be_in_the_future)
            return
        }
        onRecordChangeButtonClick(
            onProceed = ::onContinueClickDelegate,
        )
    }

    fun onDuplicateClick() {
        onRecordChangeButtonClick(
            onProceed = ::onDuplicateClickDelegate,
        )
    }

    fun onMergeClick() {
        onRecordChangeButtonClick(
            onProceed = {
                changeRecordMergeDelegate.onMergeClickDelegate(
                    prevRecord = prevRecord,
                    newTimeEnded = newTimeEnded,
                    onMergeComplete = {
                        router.back()
                    }
                )
            },
            checkTypeSelected = false,
        )
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (item.id != newTypeId) {
                newTypeId = item.id
                newCategoryIds.clear()
                updatePreview()
                updateCategoriesViewData()
                updateLastCommentsViewData()
                updateTimeSplitData()
                updateMergeData()
            }

            // Close type selection after type is selected
            onTypeChooserClick()
            // If type has any record tags - open tag selection
            if (recordTagInteractor.getByType(newTypeId).isNotEmpty()) {
                delay(300)
                onCategoryChooserClick()
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
            data = getChangeCategoryParams(
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
            data = getChangeCategoryParams(
                ChangeTagData.New(preselectedTypeId)
            )
        )
    }

    fun onCommentClick(item: ChangeRecordCommentViewData) {
        // View update through text change listener.
        comment.set(item.text)
    }

    fun onCommentChange(comment: String) {
        viewModelScope.launch {
            if (comment != newComment) {
                newComment = comment
                updatePreview()
                updateFavCommentViewData()
            }
        }
    }

    fun onSearchCommentChange(search: String) {
        val isEnabled = searchCommentViewData.value?.enabled ?: return

        if (search != searchComment && isEnabled) {
            searchComment = search
            searchLoadJob?.cancel()
            searchLoadJob = viewModelScope.launch {
                updateSearchCommentViewData(isLoading = true, isEnabled = true)
                updateSearchCommentViewData(isLoading = false, isEnabled = true)
            }
        }
    }

    fun onFavouriteCommentClick() {
        if (newComment.isEmpty()) return

        viewModelScope.launch {
            favouriteCommentInteractor.get(newComment)
                ?.let { favouriteCommentInteractor.remove(it.id) }
                ?: run {
                    val new = FavouriteComment(comment = newComment)
                    favouriteCommentInteractor.add(new)
                }
            updateLastCommentsViewData()
            updateFavCommentViewData()
        }
    }

    fun onSearchCommentClick() {
        val currentlyEnabled = searchCommentViewData.value?.enabled.orFalse()

        keyboardVisibility.set(false)
        viewModelScope.launch {
            updateSearchCommentViewData(
                isEnabled = !currentlyEnabled,
                isLoading = false
            )
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModelScope.launch {
            when (tag) {
                TIME_STARTED_TAG -> {
                    if (timestamp != newTimeStarted) {
                        newTimeStarted = timestamp
                        onTimeStartedChanged()
                    }
                }
                TIME_ENDED_TAG -> {
                    if (timestamp != newTimeEnded) {
                        newTimeEnded = timestamp
                        onTimeEndedChanged()
                    }
                }
                TIME_SPLIT_TAG -> {
                    if (timestamp != newTimeSplit) {
                        newTimeSplit = timestamp
                        onTimeSplitChanged()
                    }
                }
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
        viewModelScope.launch {
            when (viewData) {
                is TimeAdjustmentView.ViewData.Now -> onAdjustTimeNowClick()
                is TimeAdjustmentView.ViewData.Adjust -> adjustRecordTime(viewData.value)
            }
        }
    }

    fun onAdjustTimeSplitItemClick(viewData: TimeAdjustmentView.ViewData) {
        viewModelScope.launch {
            when (viewData) {
                is TimeAdjustmentView.ViewData.Now -> {
                    newTimeSplit = System.currentTimeMillis()
                    onTimeSplitChanged()
                }
                is TimeAdjustmentView.ViewData.Adjust -> {
                    newTimeSplit += TimeUnit.MINUTES.toMillis(viewData.value)
                    onTimeSplitChanged()
                }
            }
        }
    }

    fun onUntrackedHintCloseClick() {
        viewModelScope.launch {
            prefsInteractor.setUntrackedTimeHintWasHidden(wasHidden = true)
            updateUntrackedTimeHintVisibility()
        }
    }

    private suspend fun updateMergeData() {
        changeRecordMergeDelegate.updateMergePreviewViewData(
            mergeAvailable = mergeAvailable,
            prevRecord = prevRecord,
            newTimeEnded = newTimeEnded,
        )
    }

    private suspend fun updateAdjustData() {
        changeRecordAdjustDelegate.updateAdjustPreviewViewData(
            adjustNextRecordAvailable = adjustNextRecordAvailable,
            prevRecord = prevRecord,
            nextRecord = nextRecord,
            newTimeStarted = newTimeStarted,
            newTimeEnded = newTimeEnded
        )
    }

    private fun onRecordChangeButtonClick(
        onProceed: suspend () -> Unit,
        checkTypeSelected: Boolean = true,
    ) {
        if (checkTypeSelected && newTypeId == 0L) {
            showMessage(R.string.change_record_message_choose_type)
            return
        }
        viewModelScope.launch {
            saveButtonEnabled.set(false)
            onProceed()
        }
    }

    private fun onNewChooserState(
        state: ChangeRecordChooserState.State,
    ) {
        val current = chooserState.value?.current ?: ChangeRecordChooserState.State.Closed
        val newState = if (current == state) {
            ChangeRecordChooserState.State.Closed
        } else {
            state
        }

        // Show keyboard on comment chooser opened, hide otherwise.
        val showKeyboard = newState is ChangeRecordChooserState.State.Comment &&
            !searchCommentViewData.value?.enabled.orFalse()
        keyboardVisibility.set(showKeyboard)

        chooserState.set(
            ChangeRecordChooserState(
                current = newState,
                previous = current,
            )
        )
    }

    private fun onTimeClick(
        tag: String,
        timestamp: Long,
    ) = viewModelScope.launch {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val showSeconds = prefsInteractor.getShowSeconds()

        router.navigate(
            DateTimeDialogParams(
                tag = tag,
                timestamp = timestamp,
                type = DateTimeDialogType.DATETIME(),
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
                showSeconds = showSeconds,
            )
        )
    }

    protected fun showMessage(stringResId: Int) {
        val params = SnackBarParams(
            message = resourceRepo.getString(stringResId),
            duration = SnackBarParams.Duration.Short,
            margins = SnackBarParams.Margins(
                bottom = resourceRepo.getDimenInDp(R.dimen.button_height),
            )
        )
        router.show(params)
    }

    private suspend fun onAdjustTimeNowClick() {
        when (timeAdjustmentState.value) {
            TimeAdjustmentState.TIME_STARTED -> {
                newTimeStarted = System.currentTimeMillis()
                onTimeStartedChanged()
            }
            TimeAdjustmentState.TIME_ENDED -> {
                newTimeEnded = System.currentTimeMillis()
                onTimeEndedChanged()
            }
            else -> {
                // Do nothing, it's hidden.
            }
        }
    }

    private suspend fun adjustRecordTime(shiftInMinutes: Long) {
        when (timeAdjustmentState.value) {
            TimeAdjustmentState.TIME_STARTED -> {
                newTimeStarted += TimeUnit.MINUTES.toMillis(shiftInMinutes)
                onTimeStartedChanged()
            }
            TimeAdjustmentState.TIME_ENDED -> {
                newTimeEnded += TimeUnit.MINUTES.toMillis(shiftInMinutes)
                onTimeEndedChanged()
            }
            else -> {
                // Do nothing, it's hidden.
            }
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

    private suspend fun onTimeSplitChanged() {
        newTimeSplit = newTimeSplit.coerceIn(newTimeStarted..splitPreviewTimeEnded)
        updateTimeSplitData()
    }

    private suspend fun initializeActions() {
        initializePrevNextRecords()
        updateTimeSplitData()
        updateMergeData()
        updateAdjustData()
    }

    private suspend fun initializePrevNextRecords() {
        prevRecord = recordInteractor.getPrev(timeStarted = originalTimeStarted, limit = 1).firstOrNull()
        nextRecord = recordInteractor.getNext(timeEnded = originalTimeEnded)
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return recordTypesViewDataInteractor.getTypesViewData()
    }

    protected suspend fun updateCategoriesViewData() {
        val data = loadCategoriesViewData()
        categories.set(data)
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return recordTagViewDataInteractor.getViewData(
            selectedTags = newCategoryIds,
            typeId = newTypeId,
            multipleChoiceAvailable = true,
            showAddButton = true,
            showArchived = false,
            showUntaggedButton = true,
        )
    }

    private fun loadTimeAdjustmentItems(): List<ViewHolderType> {
        return changeRecordViewDataInteractor.getTimeAdjustmentItems()
    }

    private suspend fun updateLastCommentsViewData() {
        lastComments.set(loadLastCommentsViewData())
    }

    private suspend fun loadLastCommentsViewData(): List<ViewHolderType> {
        return changeRecordViewDataInteractor.getLastCommentsViewData(newTypeId)
    }

    private suspend fun updateFavCommentViewData() {
        favCommentViewData.set(loadFavCommentViewData())
    }

    private suspend fun loadFavCommentViewData(): ChangeRecordFavCommentState {
        return changeRecordViewDataInteractor.getFavCommentViewData(newComment)
    }

    private suspend fun updateSearchCommentViewData(
        isLoading: Boolean,
        isEnabled: Boolean,
    ) {
        val data = loadSearchCommentViewData(
            isLoading = isLoading,
            isEnabled = isEnabled
        )
        searchCommentViewData.set(data)
    }

    private suspend fun loadSearchCommentViewData(
        isLoading: Boolean,
        isEnabled: Boolean,
    ): ChangeRecordSearchCommentState {
        return changeRecordViewDataInteractor.getSearchCommentViewData(
            isEnabled = isEnabled,
            isLoading = isLoading,
            search = searchComment,
        )
    }

    private suspend fun updateUntrackedTimeHintVisibility() {
        untrackedTimeHintVisibility.set(loadUntrackedTimeHintVisibility())
    }

    private suspend fun loadUntrackedTimeHintVisibility(): Boolean {
        val wasHidden = prefsInteractor.getUntrackedTimeHintWasHidden()
        val isCalendarView = prefsInteractor.getShowRecordsCalendar()
        return untrackedHintAvailable && isCalendarView && !wasHidden
    }

    companion object {
        const val TIME_STARTED_TAG = "time_started_tag"
        const val TIME_ENDED_TAG = "time_ended_tag"
        const val TIME_SPLIT_TAG = "time_split_tag"
    }
}