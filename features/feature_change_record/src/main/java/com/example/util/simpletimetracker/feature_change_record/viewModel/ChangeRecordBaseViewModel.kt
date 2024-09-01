package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.model.FavouriteComment
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordButtonViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordCommentViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeAdjustmentViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeDoublePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeField
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.model.TimeAdjustmentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordFavCommentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSearchCommentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordTagsViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

abstract class ChangeRecordBaseViewModel(
    private val router: Router,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val recordInteractor: RecordInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val changeRecordActionsDelegate: ChangeRecordActionsDelegateImpl,
) : ViewModel(),
    ChangeRecordActionsDelegate by changeRecordActionsDelegate {

    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadTypesViewData() }
            initial
        }
    }
    val categories: LiveData<ChangeRecordTagsViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTagsViewData>().let { initial ->
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
    val timeStartedAdjustmentItems: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(loadTimeAdjustmentItems(ChangeRecordDateTimeField.START))
    }
    val timeEndedAdjustmentItems: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(loadTimeAdjustmentItems(ChangeRecordDateTimeField.END))
    }
    val chooserState: LiveData<ChangeRecordChooserState> = MutableLiveData(
        ChangeRecordChooserState(
            current = ChangeRecordChooserState.State.Closed,
            previous = ChangeRecordChooserState.State.Closed,
        ),
    )
    val searchCommentViewData: LiveData<ChangeRecordSearchCommentState> by lazy {
        return@lazy MutableLiveData<ChangeRecordSearchCommentState>().let { initial ->
            viewModelScope.launch {
                initial.value = loadSearchCommentViewData(isLoading = false, isEnabled = false)
            }
            initial
        }
    }
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)
    val comment: LiveData<String> = MutableLiveData()
    val favCommentViewData: LiveData<ChangeRecordFavCommentState> = MutableLiveData()
    val timeEndedVisibility: LiveData<Boolean> by lazy { MutableLiveData(isTimeEndedAvailable) }
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(isDeleteButtonVisible) }
    val statsIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(isStatisticsButtonVisible) }

    protected var newTypeId: Long = 0
    protected var newTimeEnded: Long = 0
    protected var newTimeStarted: Long = 0
    protected var newTimeSplit: Long = 0
    protected var newComment: String = ""
    protected var newCategoryIds: MutableList<Long> = mutableListOf()
    protected var originalRecordId: Long = 0
    protected var originalTypeId: Long = 0
    protected var originalTimeStarted: Long = 0
    protected var originalTimeEnded: Long = 0
    protected var dateTimeState = ChangeRecordDateTimeFieldsState(
        start = ChangeRecordDateTimeFieldsState.State.DateTime,
        end = ChangeRecordDateTimeFieldsState.State.DateTime,
    )

    protected abstract suspend fun updatePreview()
    protected abstract fun getChangeCategoryParams(data: ChangeTagData): ChangeRecordTagFromScreen
    protected abstract suspend fun onSaveClickDelegate()
    protected open suspend fun sendPreviewUpdate(fullUpdate: Boolean) {}
    protected abstract val forceSecondsInDurationDialog: Boolean
    protected abstract val mergeAvailable: Boolean
    protected abstract val previewTimeEnded: Long
    protected abstract val showTimeEndedOnSplitPreview: Boolean
    protected abstract val adjustPreviewTimeEnded: Long
    protected abstract val adjustPreviewOriginalTimeEnded: Long
    protected abstract val showTimeEndedOnAdjustPreview: Boolean
    protected abstract val adjustNextRecordAvailable: Boolean
    protected abstract val isTimeEndedAvailable: Boolean
    protected abstract val isAdditionalActionsAvailable: Boolean
    protected abstract val isDeleteButtonVisible: Boolean
    protected abstract val isStatisticsButtonVisible: Boolean

    private var prevRecord: Record? = null
    private var searchComment: String = ""
    private var searchLoadJob: Job? = null

    init {
        changeRecordActionsDelegate.attach(getActionsDelegateParent())
    }

    override fun onCleared() {
        changeRecordActionsDelegate.clear()
        super.onCleared()
    }

    protected open suspend fun initializePreviewViewData() {
        // Don't wait for the completion.
        viewModelScope.launch { initializeActions() }
        viewModelScope.launch { updateFavCommentViewData() }
    }

    protected open suspend fun onTimeStartedChanged() {
        updatePreview()
        updateActionsData()
    }

    protected open suspend fun onTimeEndedChanged() {
        updatePreview()
        updateActionsData()
    }

    protected fun updateActionsData() {
        changeRecordActionsDelegate.updateData()
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
        val tag = TIME_STARTED_TAG
        when (dateTimeState.start) {
            is ChangeRecordDateTimeFieldsState.State.DateTime -> onTimeClick(
                tag = tag,
                timestamp = newTimeStarted,
            )
            is ChangeRecordDateTimeFieldsState.State.Duration -> onDurationClick(
                tag = tag,
                durationMillis = previewTimeEnded - newTimeStarted,
            )
        }
    }

    fun onTimeEndedClick() {
        val tag = TIME_ENDED_TAG
        when (dateTimeState.end) {
            is ChangeRecordDateTimeFieldsState.State.DateTime -> onTimeClick(
                tag = tag,
                timestamp = newTimeEnded,
            )
            is ChangeRecordDateTimeFieldsState.State.Duration -> onDurationClick(
                tag = tag,
                durationMillis = previewTimeEnded - newTimeStarted,
            )
        }
    }

    fun onTimeStartedStateClick() {
        onTimeStateClick(field = ChangeRecordDateTimeField.START)
    }

    fun onTimeEndedStateClick() {
        onTimeStateClick(field = ChangeRecordDateTimeField.END)
    }

    fun onItemTimePreviewClick(data: ChangeRecordTimePreviewViewData) {
        when (data.block) {
            ChangeRecordActionsBlock.SplitTimePreview ->
                onTimeSplitClick()
            else -> {
                // Do nothing.
            }
        }
    }

    fun onItemTimeStartedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        when (data.block) {
            ChangeRecordActionsBlock.AdjustTimePreview ->
                onTimeStartedClick()
            else -> {
                // Do nothing.
            }
        }
    }

    fun onItemTimeEndedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        when (data.block) {
            ChangeRecordActionsBlock.AdjustTimePreview ->
                onTimeEndedClick()
            else -> {
                // Do nothing.
            }
        }
    }

    fun onItemAdjustTimeStartedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        changeRecordActionsDelegate.onItemAdjustTimeStartedClick(data)
    }

    fun onItemAdjustTimeEndedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        changeRecordActionsDelegate.onItemAdjustTimeEndedClick(data)
    }

    fun onChangePreviewCheckClick(item: ChangeRecordChangePreviewViewData) {
        changeRecordActionsDelegate.onChangePreviewCheckClick(item)
    }

    fun onSaveClick() {
        onRecordChangeButtonClick(
            onProceed = ::onSaveClickDelegate,
        )
    }

    fun onItemButtonClick(viewData: ChangeRecordButtonViewData) {
        changeRecordActionsDelegate.onItemButtonClick(viewData)
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (item.id != newTypeId) {
                newTypeId = item.id
                newCategoryIds.clear()
                viewModelScope.launch {
                    updatePreview()
                    updateCategoriesViewData()
                    updateLastCommentsViewData()
                }
                updateActionsData()
            }

            // Close type selection after type is selected
            onTypeChooserClick()
            // If type has any record tags - open tag selection
            if (recordTypeToTagInteractor.getTags(newTypeId).isNotEmpty()) {
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
                    ),
                ),
            ),
            sharedElements = mapOf(sharedElements),
        )
    }

    fun onAddCategoryClick() {
        val preselectedTypeId: Long? = newTypeId.takeUnless { it == 0L }
        router.navigate(
            data = getChangeCategoryParams(
                ChangeTagData.New(preselectedTypeId),
            ),
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
                isLoading = false,
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

    fun onDurationSet(durationSeconds: Long, tag: String?) {
        viewModelScope.launch {
            when (tag) {
                TIME_STARTED_TAG -> {
                    newTimeStarted = previewTimeEnded - durationSeconds * 1000
                    onTimeStartedChanged()
                }
                TIME_ENDED_TAG -> {
                    newTimeEnded = newTimeStarted + durationSeconds * 1000
                    onTimeEndedChanged()
                }
            }
        }
    }

    fun onTimeAdjustmentClick(
        data: ChangeRecordTimeAdjustmentViewData,
        viewData: TimeAdjustmentView.ViewData,
    ) {
        when (data.block) {
            ChangeRecordActionsBlock.SplitTimeAdjustment ->
                onAdjustTimeSplitItemClick(viewData)
            ChangeRecordActionsBlock.AdjustTimeAdjustment ->
                onAdjustTimeChangeClick(viewData)
            else -> {
                // Do nothing.
            }
        }
    }

    fun onAdjustTimeStartedItemClick(viewData: TimeAdjustmentView.ViewData) {
        onAdjustTimeItemClick(TimeAdjustmentState.TIME_STARTED, viewData)
    }

    fun onAdjustTimeEndedItemClick(viewData: TimeAdjustmentView.ViewData) {
        onAdjustTimeItemClick(TimeAdjustmentState.TIME_ENDED, viewData)
    }

    fun onBackPressed() {
        if (chooserState.value?.current !is ChangeRecordChooserState.State.Closed) {
            onNewChooserState(ChangeRecordChooserState.State.Closed)
        } else {
            viewModelScope.launch {
                // Send only if not changed and update only time.
                if (newTimeStarted == originalTimeStarted) {
                    sendPreviewUpdate(fullUpdate = false)
                }
                router.back()
            }
        }
    }

    private fun onAdjustTimeChangeClick(viewData: TimeAdjustmentView.ViewData) {
        when (changeRecordActionsDelegate.timeChangeAdjustmentState) {
            TimeAdjustmentState.TIME_STARTED -> {
                onAdjustTimeItemClick(TimeAdjustmentState.TIME_STARTED, viewData)
            }
            TimeAdjustmentState.TIME_ENDED -> {
                onAdjustTimeItemClick(TimeAdjustmentState.TIME_ENDED, viewData)
            }
            else -> {
                // Do nothing, it's hidden.
            }
        }
    }

    private fun onTimeSplitClick() {
        onTimeClick(tag = TIME_SPLIT_TAG, timestamp = newTimeSplit)
    }

    private fun onAdjustTimeSplitItemClick(viewData: TimeAdjustmentView.ViewData) {
        viewModelScope.launch {
            when (viewData) {
                is TimeAdjustmentView.ViewData.Now -> {
                    newTimeSplit = System.currentTimeMillis()
                    onTimeSplitChanged()
                }
                is TimeAdjustmentView.ViewData.Zero -> {
                    // Do nothing, shouldn't be there.
                }
                is TimeAdjustmentView.ViewData.Adjust -> {
                    newTimeSplit += TimeUnit.MINUTES.toMillis(viewData.value)
                    onTimeSplitChanged()
                }
            }
        }
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
            val canProceed = saveButtonEnabled.value.orFalse()
            if (!canProceed) return@launch
            saveButtonEnabled.set(false)
            updateActionsData()
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
            ),
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
            ),
        )
    }

    private fun onDurationClick(
        tag: String,
        durationMillis: Long,
    ) = viewModelScope.launch {
        val showSeconds = prefsInteractor.getShowSeconds()

        router.navigate(
            DurationDialogParams(
                tag = tag,
                value = DurationDialogParams.Value.DurationSeconds(
                    duration = durationMillis / 1000,
                ),
                hideDisableButton = true,
                showSeconds = forceSecondsInDurationDialog || showSeconds,
            ),
        )
    }

    private fun onTimeStateClick(
        field: ChangeRecordDateTimeField,
    ) = viewModelScope.launch {
        val current = dateTimeState

        fun ChangeRecordDateTimeFieldsState.State.flip(): ChangeRecordDateTimeFieldsState.State {
            return when (this) {
                is ChangeRecordDateTimeFieldsState.State.DateTime ->
                    ChangeRecordDateTimeFieldsState.State.Duration
                is ChangeRecordDateTimeFieldsState.State.Duration ->
                    ChangeRecordDateTimeFieldsState.State.DateTime
            }
        }

        // Can't select duration on both fields.
        // Keep clicked state, change other to DateTime.
        fun adjustState(
            currentState: ChangeRecordDateTimeFieldsState,
        ): ChangeRecordDateTimeFieldsState {
            return ChangeRecordDateTimeFieldsState(
                start = when (field) {
                    ChangeRecordDateTimeField.START -> currentState.start
                    ChangeRecordDateTimeField.END -> ChangeRecordDateTimeFieldsState.State.DateTime
                },
                end = when (field) {
                    ChangeRecordDateTimeField.START -> ChangeRecordDateTimeFieldsState.State.DateTime
                    ChangeRecordDateTimeField.END -> currentState.end
                },
            )
        }

        val newStart = when (field) {
            ChangeRecordDateTimeField.START -> current.start.flip()
            ChangeRecordDateTimeField.END -> current.start
        }
        val newEnd = when (field) {
            ChangeRecordDateTimeField.START -> current.end
            ChangeRecordDateTimeField.END -> current.end.flip()
        }

        dateTimeState = ChangeRecordDateTimeFieldsState(
            start = newStart,
            end = newEnd,
        )

        if (dateTimeState.start == dateTimeState.end) {
            dateTimeState = adjustState(dateTimeState)
            updateTimeAdjustmentItems(ChangeRecordDateTimeField.START)
            updateTimeAdjustmentItems(ChangeRecordDateTimeField.END)
        } else {
            updateTimeAdjustmentItems(field)
        }

        updatePreview()
    }

    protected fun showMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }

    private fun onAdjustTimeItemClick(
        state: TimeAdjustmentState,
        viewData: TimeAdjustmentView.ViewData,
    ) {
        viewModelScope.launch {
            when (viewData) {
                is TimeAdjustmentView.ViewData.Now -> onAdjustTimeNowClick(state)
                is TimeAdjustmentView.ViewData.Zero -> onAdjustTimeZeroClick(state)
                is TimeAdjustmentView.ViewData.Adjust -> adjustRecordTime(state, viewData.value)
            }
        }
    }

    private suspend fun onAdjustTimeNowClick(
        state: TimeAdjustmentState,
    ) {
        when (state) {
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

    private suspend fun onAdjustTimeZeroClick(
        state: TimeAdjustmentState,
    ) {
        when (state) {
            TimeAdjustmentState.TIME_STARTED -> {
                newTimeStarted = previewTimeEnded
                onTimeStartedChanged()
            }
            TimeAdjustmentState.TIME_ENDED -> {
                newTimeEnded = newTimeStarted
                onTimeEndedChanged()
            }
            else -> {
                // Do nothing, it's hidden.
            }
        }
    }

    private suspend fun adjustRecordTime(
        state: TimeAdjustmentState,
        shiftInMinutes: Long,
    ) {
        val shift = TimeUnit.MINUTES.toMillis(shiftInMinutes)
        when (state) {
            TimeAdjustmentState.TIME_STARTED -> {
                when (dateTimeState.start) {
                    is ChangeRecordDateTimeFieldsState.State.DateTime -> {
                        newTimeStarted += shift
                    }
                    is ChangeRecordDateTimeFieldsState.State.Duration -> {
                        newTimeStarted -= shift
                    }
                }
                onTimeStartedChanged()
            }
            TimeAdjustmentState.TIME_ENDED -> {
                when (dateTimeState.end) {
                    is ChangeRecordDateTimeFieldsState.State.DateTime -> {
                        newTimeEnded += shift
                    }
                    is ChangeRecordDateTimeFieldsState.State.Duration -> {
                        newTimeEnded = previewTimeEnded + shift
                    }
                }
                onTimeEndedChanged()
            }
            else -> {
                // Do nothing, it's hidden.
            }
        }
    }

    private fun onTimeSplitChanged() {
        newTimeSplit = newTimeSplit.coerceIn(newTimeStarted..previewTimeEnded)
        updateActionsData()
    }

    private fun getActionsDelegateParent(): ChangeRecordActionsDelegate.Parent {
        return object : ChangeRecordActionsDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsDelegate.Parent.ViewDataParams {
                return ChangeRecordActionsDelegate.Parent.ViewDataParams(
                    baseParams = ChangeRecordActionsDelegate.Parent.ViewDataParams.BaseParams(
                        newTypeId = newTypeId,
                        newTimeStarted = newTimeStarted,
                        newTimeEnded = newTimeEnded,
                        newComment = newComment,
                        newCategoryIds = newCategoryIds,
                        isButtonEnabled = saveButtonEnabled.value.orFalse(),
                    ),
                    splitParams = ChangeRecordActionsDelegate.Parent.ViewDataParams.SplitParams(
                        newTimeSplit = newTimeSplit,
                        splitPreviewTimeEnded = previewTimeEnded,
                        showTimeEndedOnSplitPreview = showTimeEndedOnSplitPreview,
                    ),
                    duplicateParams = ChangeRecordActionsDelegate.Parent.ViewDataParams.DuplicateParams(
                        isAdditionalActionsAvailable = isAdditionalActionsAvailable,
                    ),
                    continueParams = ChangeRecordActionsDelegate.Parent.ViewDataParams.ContinueParams(
                        originalRecordId = originalRecordId,
                        isAdditionalActionsAvailable = isAdditionalActionsAvailable,
                    ),
                    repeatParams = ChangeRecordActionsDelegate.Parent.ViewDataParams.RepeatParams(
                        isAdditionalActionsAvailable = isAdditionalActionsAvailable,
                    ),
                    adjustParams = ChangeRecordActionsDelegate.Parent.ViewDataParams.AdjustParams(
                        originalRecordId = originalRecordId,
                        originalTypeId = originalTypeId,
                        originalTimeStarted = originalTimeStarted,
                        adjustNextRecordAvailable = adjustNextRecordAvailable,
                        adjustPreviewTimeEnded = adjustPreviewTimeEnded,
                        adjustPreviewOriginalTimeEnded = adjustPreviewOriginalTimeEnded,
                        showTimeEndedOnAdjustPreview = showTimeEndedOnAdjustPreview,
                        isTimeEndedAvailable = isTimeEndedAvailable,
                    ),
                    mergeParams = ChangeRecordActionsDelegate.Parent.ViewDataParams.MergeParams(
                        mergeAvailable = mergeAvailable,
                        prevRecord = prevRecord,
                    ),
                )
            }

            override fun updateViewData() {
                changeRecordActionsDelegate.updateViewData()
            }

            override fun onRecordChangeButtonClick(
                onProceed: suspend () -> Unit,
                checkTypeSelected: Boolean,
            ) {
                this@ChangeRecordBaseViewModel.onRecordChangeButtonClick(
                    onProceed = onProceed,
                    checkTypeSelected = checkTypeSelected,
                )
            }

            override suspend fun onSaveClickDelegate() {
                this@ChangeRecordBaseViewModel.onSaveClickDelegate()
            }

            override suspend fun onSplitComplete() {
                newTimeStarted = newTimeSplit
                onSaveClickDelegate()
            }

            override fun showMessage(stringResId: Int) {
                this@ChangeRecordBaseViewModel.showMessage(stringResId)
            }
        }
    }

    private suspend fun initializeActions() {
        initializePrevRecord()
        updateActionsData()
    }

    private suspend fun initializePrevRecord() {
        prevRecord = recordInteractor.getPrev(timeStarted = originalTimeStarted).firstOrNull()
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return recordTypesViewDataInteractor.getTypesViewData()
    }

    protected suspend fun updateCategoriesViewData() {
        val data = loadCategoriesViewData()
        categories.set(data)
    }

    private suspend fun loadCategoriesViewData(): ChangeRecordTagsViewData {
        return recordTagViewDataInteractor.getViewData(
            selectedTags = newCategoryIds,
            typeId = newTypeId,
            multipleChoiceAvailable = true,
            showAddButton = true,
            showArchived = false,
            showUntaggedButton = true,
        ).let {
            ChangeRecordTagsViewData(
                selectedCount = it.selectedCount,
                viewData = it.data,
            )
        }
    }

    private fun updateTimeAdjustmentItems(
        field: ChangeRecordDateTimeField,
    ) {
        val data = loadTimeAdjustmentItems(field)
        when (field) {
            ChangeRecordDateTimeField.START -> timeStartedAdjustmentItems.set(data)
            ChangeRecordDateTimeField.END -> timeEndedAdjustmentItems.set(data)
        }
    }

    private fun loadTimeAdjustmentItems(
        field: ChangeRecordDateTimeField,
    ): List<ViewHolderType> {
        val state = when (field) {
            ChangeRecordDateTimeField.START -> dateTimeState.start
            ChangeRecordDateTimeField.END -> dateTimeState.end
        }
        return changeRecordViewDataInteractor.getTimeAdjustmentItems(
            dateTimeFieldState = state,
        )
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
            isEnabled = isEnabled,
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

    companion object {
        const val TIME_STARTED_TAG = "time_started_tag"
        const val TIME_ENDED_TAG = "time_ended_tag"
        const val TIME_SPLIT_TAG = "time_split_tag"
    }
}