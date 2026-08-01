package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.dropSeconds
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.NeedTagValueSelectionInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryAddViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordSliderViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeAdjustmentViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeDoublePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeField
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.model.TimeAdjustmentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordChooserState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordTagsViewData
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordDelegateBridge
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordDelegateBridge.Action
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordDelegateBridge.ViewDataParams
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordEditorDelegate
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordEditorMode
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordEditorState
import com.example.util.simpletimetracker.feature_change_record.viewModel.delegates.ChangeRecordActionsMoveDelegate
import com.example.util.simpletimetracker.feature_comment_selection.api.CommentSelectionViewModelDelegate
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChangeRecordEditorDelegateImpl @Inject constructor(
    override val commentSelectionViewModelDelegate: CommentSelectionViewModelDelegate,
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val recordInteractor: RecordInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val changeRecordActionsDelegate: ChangeRecordActionsDelegateImpl,
    private val needTagValueSelectionInteractor: NeedTagValueSelectionInteractor,
) : ChangeRecordEditorDelegate,
    ViewModelDelegate(),
    CommentSelectionViewModelDelegate by commentSelectionViewModelDelegate {

    override val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            delegateScope.launch { initial.value = loadTypesViewData() }
            initial
        }
    }
    override val categories: LiveData<ChangeRecordTagsViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTagsViewData>().let { initial ->
            delegateScope.launch {
                mode.initializePreviewViewData()
                initial.value = loadCategoriesViewData(fromSearchChange = false)
            }
            initial
        }
    }
    override val timeStartedAdjustmentItems: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(loadTimeAdjustmentItems(ChangeRecordDateTimeField.START))
    }
    override val timeEndedAdjustmentItems: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(loadTimeAdjustmentItems(ChangeRecordDateTimeField.END))
    }
    override val chooserState: LiveData<ChangeRecordChooserState> = MutableLiveData(
        ChangeRecordChooserState(
            current = ChangeRecordChooserState.State.Closed,
            previous = ChangeRecordChooserState.State.Closed,
        ),
    )
    override val actionsViewData: LiveData<List<ViewHolderType>>
        by changeRecordActionsDelegate::actionsViewData
    override val saveButtonEnabled: LiveData<Boolean> =
        MutableLiveData(true)
    override val keyboardVisibility: LiveData<Boolean> =
        MutableLiveData(false)
    override val timeEndedVisibility: LiveData<Boolean>
        by lazy { MutableLiveData(mode.config.isTimeEndedAvailable) }
    override val deleteIconVisibility: LiveData<Boolean>
        by lazy { MutableLiveData(mode.config.isDeleteButtonVisible) }
    override val statsIconVisibility: LiveData<Boolean>
        by lazy { MutableLiveData(mode.config.isStatisticsButtonVisible) }

    override val recordState = ChangeRecordEditorState()
    override var dateTimeState = ChangeRecordDateTimeFieldsState(
        start = ChangeRecordDateTimeFieldsState.State.DateTime,
        end = ChangeRecordDateTimeFieldsState.State.DateTime,
    )

    private lateinit var mode: ChangeRecordEditorMode
    private var prevRecord: Record? = null
    private var tagSearchJob: Job? = null
    private var tagSearchText: String = ""

    init {
        val bridge = ChangeRecordDelegateBridge(
            actionConsumer = getDelegateActionsConsumer(),
            paramsProvider = getDelegateParamsProvider(),
        )
        changeRecordActionsDelegate.attach(bridge)
        commentSelectionViewModelDelegate.attach(getCommentSelectionDelegateParent())
    }

    override fun attach(mode: ChangeRecordEditorMode) {
        this.mode = mode
    }

    override fun clear() {
        changeRecordActionsDelegate.clear()
        // TODO BASE clear comment delegate?
        super.clear()
    }

    override fun onDeleteClick() {
        mode.onDeleteClick()
    }

    override fun onStatisticsClick() {
        mode.onStatisticsClick()
    }

    override fun afterInitializePreviewViewData() {
        recordState.newTimeSplit = recordState.newTimeStarted
        recordState.originalTypeId = recordState.newTypeId
        recordState.originalTags = recordState.newTags.toList() // Creates a copy.
        recordState.originalTimeStarted = recordState.newTimeStarted
        recordState.originalTimeEnded = recordState.newTimeEnded
        commentSelectionViewModelDelegate.updateCommentsViewData()
        // Don't wait for the completion.
        delegateScope.launch { initializeActions() }
    }

    override suspend fun afterTimeStartedChanged() {
        if (recordState.newTimeStarted > recordState.newTimeSplit) {
            recordState.newTimeSplit = recordState.newTimeStarted
        }
        mode.updatePreview()
        updateActionsData()
    }

    override suspend fun afterTimeEndedChanged() {
        if (recordState.newTimeEnded < recordState.newTimeSplit) {
            recordState.newTimeSplit = recordState.newTimeEnded
        }
        mode.updatePreview()
        updateActionsData()
    }

    override fun updateActionsData() {
        changeRecordActionsDelegate.updateData()
    }

    override fun onVisible() {
        delegateScope.launch {
            updateCategoriesViewData()
        }
    }

    override fun onTypeChooserClick() {
        onNewChooserState(ChangeRecordChooserState.State.Activity)
    }

    override fun onCategoryChooserClick() {
        onNewChooserState(ChangeRecordChooserState.State.Tag)
    }

    override fun onCommentChooserClick() {
        onNewChooserState(ChangeRecordChooserState.State.Comment)
    }

    override fun onActionChooserClick() {
        onNewChooserState(ChangeRecordChooserState.State.Action)
    }

    override fun onTimeStartedClick() {
        val tag = TIME_STARTED_TAG
        when (dateTimeState.start) {
            is ChangeRecordDateTimeFieldsState.State.DateTime -> onTimeClick(
                tag = tag,
                timestamp = recordState.newTimeStarted,
            )
            is ChangeRecordDateTimeFieldsState.State.Duration -> onDurationClick(
                tag = tag,
                durationMillis = mode.previewTimeEnded() - recordState.newTimeStarted,
            )
        }
    }

    override fun onTimeEndedClick() {
        val tag = TIME_ENDED_TAG
        when (dateTimeState.end) {
            is ChangeRecordDateTimeFieldsState.State.DateTime -> onTimeClick(
                tag = tag,
                timestamp = recordState.newTimeEnded,
            )
            is ChangeRecordDateTimeFieldsState.State.Duration -> onDurationClick(
                tag = tag,
                durationMillis = mode.previewTimeEnded() - recordState.newTimeStarted,
            )
        }
    }

    override fun onTimeStartedStateClick() {
        onTimeStateClick(field = ChangeRecordDateTimeField.START)
    }

    override fun onTimeEndedStateClick() {
        onTimeStateClick(field = ChangeRecordDateTimeField.END)
    }

    override fun onItemTimePreviewClick(data: ChangeRecordTimePreviewViewData) {
        when (data.block) {
            ChangeRecordActionsBlock.SplitTimePreview ->
                onTimeSplitClick()
            else -> {
                // Do nothing.
            }
        }
    }

    override fun onItemTimeStartedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        when (data.block) {
            ChangeRecordActionsBlock.AdjustTimePreview ->
                onTimeStartedClick()
            else -> {
                // Do nothing.
            }
        }
    }

    override fun onItemTimeEndedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        when (data.block) {
            ChangeRecordActionsBlock.AdjustTimePreview ->
                onTimeEndedClick()
            else -> {
                // Do nothing.
            }
        }
    }

    override fun onItemAdjustTimeStartedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        changeRecordActionsDelegate.onItemAdjustTimeStartedClick(data)
    }

    override fun onItemAdjustTimeEndedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        changeRecordActionsDelegate.onItemAdjustTimeEndedClick(data)
    }

    override fun onChangePreviewCheckClick(item: ChangeRecordChangePreviewViewData) {
        changeRecordActionsDelegate.onChangePreviewCheckClick(item)
    }

    override fun onChangePreviewBeforeActionClick() {
        openSplitTypeSelection(tag = SPLIT_BEFORE_TYPE_SELECTION)
    }

    override fun onChangePreviewAfterActionClick() {
        openSplitTypeSelection(tag = SPLIT_AFTER_TYPE_SELECTION)
    }

    override fun onSaveClick() {
        onRecordChangeButtonClick(
            onProceed = { mode.onSaveClickDelegate {} },
        )
    }

    override fun onItemButtonClick(viewData: ButtonViewData) {
        changeRecordActionsDelegate.onItemButtonClick(viewData)
    }

    override fun onTypeClick(item: RecordTypeViewData) {
        delegateScope.launch {
            onMainTypeSelected(item.id)
            // Close type selection after type is selected
            onTypeChooserClick()
            openTagSelectionIfNeeded()
        }
    }

    override fun onCategoryClick(item: CategoryViewData) {
        delegateScope.launch {
            when (item) {
                is CategoryViewData.Record.Tagged -> {
                    val needValueSelection = needTagValueSelectionInteractor.execute(
                        selectedTagIds = recordState.newTags.map { it.tagId },
                        clickedTagId = item.id,
                    )
                    if (needValueSelection) {
                        RecordTagValueSelectionParams(
                            tag = CHANGE_RECORD_TAG_VALUE_SELECTION,
                            tagId = item.id,
                        ).let(router::navigate)
                    } else {
                        recordState.newTags = recordState.newTags.addOrRemove(item.id)
                    }
                }
                is CategoryViewData.Record.Untagged -> {
                    recordState.newTags = emptyList()
                }
                else -> return@launch
            }
            mode.updatePreview()
            updateCategoriesViewData()
        }
    }

    override fun onCategoryValueSelected(
        params: RecordTagValueSelectionParams,
        value: Double,
    ) {
        if (params.tag != CHANGE_RECORD_TAG_VALUE_SELECTION) return
        delegateScope.launch {
            recordState.newTags = recordState.newTags + RecordBase.Tag(
                tagId = params.tagId,
                numericValue = value,
            )
            mode.updatePreview()
            updateCategoriesViewData()
        }
    }

    override fun onDataSelected(
        tag: String?,
        dataIds: List<Long>,
    ) {
        when (tag) {
            SPLIT_BEFORE_TYPE_SELECTION -> {
                val selectedTypeId = dataIds.firstOrNull() ?: return
                recordState.newSplitBeforeTypeId = selectedTypeId
                updateActionsData()
            }
            SPLIT_AFTER_TYPE_SELECTION -> {
                val selectedTypeId = dataIds.firstOrNull() ?: return
                if (selectedTypeId != recordState.newTypeId) {
                    if (recordState.newSplitBeforeTypeId == null) {
                        recordState.newSplitBeforeTypeId = recordState.newTypeId
                    }
                    onMainTypeSelected(selectedTypeId)
                }
            }
            else -> {
                commentSelectionViewModelDelegate.onDelegateDataSelected(tag, dataIds)
            }
        }
    }

    override fun onCategoryLongClick(item: CategoryViewData, sharedElements: Pair<Any, String>) {
        val icon = (item as? CategoryViewData.Record)?.icon?.toParams()

        router.navigate(
            data = mode.getChangeCategoryParams(
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

    override fun onCategorySpecialClick(viewData: CategoryAddViewData) {
        when (viewData.type) {
            is CategoryAddViewData.Type.AddTag -> {
                val preselectedTypeId: Long? = recordState.newTypeId.takeUnless { it == 0L }
                router.navigate(
                    data = mode.getChangeCategoryParams(
                        ChangeTagData.New(preselectedTypeId),
                    ),
                )
            }
            is CategoryAddViewData.Type.ShowAll -> delegateScope.launch {
                val current = prefsInteractor.getIsShowAllTagsEnabled()
                prefsInteractor.setIsShowAllTagsEnabled(!current)
                updateCategoriesViewData()
            }
            is CategoryAddViewData.Type.EnableSearch -> delegateScope.launch {
                val current = prefsInteractor.getIsTagSearchEnabled()
                prefsInteractor.setIsTagSearchEnabled(!current)
                updateCategoriesViewData()
            }
        }
    }

    override fun onSearchTextChange(text: String) {
        if (text != tagSearchText) {
            tagSearchJob?.cancel()
            tagSearchJob = delegateScope.launch {
                tagSearchText = text
                // Do not delay on clear.
                if (text.isNotEmpty()) delay(500)
                updateCategoriesViewData(fromValueChange = true)
            }
        }
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        delegateScope.launch {
            val coercedTimestamp = if (prefsInteractor.getShowSeconds()) {
                timestamp
            } else {
                timestamp.dropSeconds()
            }

            when (tag) {
                TIME_STARTED_TAG -> {
                    if (coercedTimestamp != recordState.newTimeStarted) {
                        recordState.newTimeStarted = coercedTimestamp
                        mode.onTimeStartedChanged()
                    }
                }
                TIME_ENDED_TAG -> {
                    if (coercedTimestamp != recordState.newTimeEnded) {
                        recordState.newTimeEnded = coercedTimestamp
                        mode.onTimeEndedChanged()
                    }
                }
                TIME_SPLIT_TAG -> {
                    if (coercedTimestamp != recordState.newTimeSplit) {
                        recordState.newTimeSplit = coercedTimestamp
                        onTimeSplitChanged()
                    }
                }
                ChangeRecordActionsMoveDelegate.MOVE_TIME_STARTED_TAG -> {
                    onRecordChangeButtonClick(
                        onProceed = {
                            val currentDuration = (recordState.newTimeEnded - recordState.newTimeStarted)
                                .coerceAtLeast(0)
                            recordState.newTimeStarted = timestamp
                            recordState.newTimeEnded = recordState.newTimeStarted + currentDuration
                            mode.onSaveClickDelegate {}
                        },
                    )
                }
            }
        }
    }

    override fun onDurationSet(durationSeconds: Long, tag: String?) {
        delegateScope.launch {
            when (tag) {
                TIME_STARTED_TAG -> {
                    recordState.newTimeStarted = mode.previewTimeEnded() - durationSeconds * 1000
                    mode.onTimeStartedChanged()
                }
                TIME_ENDED_TAG -> {
                    recordState.newTimeEnded = recordState.newTimeStarted + durationSeconds * 1000
                    mode.onTimeEndedChanged()
                }
            }
        }
    }

    override fun onTimeAdjustmentClick(
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

    override fun onSliderValueChanged(viewData: ChangeRecordSliderViewData, value: Float) {
        when (viewData.block) {
            ChangeRecordActionsBlock.SplitSlider ->
                onSliderSplitValueChanged(value)
            else -> {
                // Do nothing.
            }
        }
    }

    override fun onAdjustTimeStartedItemClick(viewData: TimeAdjustmentView.ViewData) {
        onAdjustTimeItemClick(TimeAdjustmentState.TIME_STARTED, viewData)
    }

    override fun onAdjustTimeEndedItemClick(viewData: TimeAdjustmentView.ViewData) {
        onAdjustTimeItemClick(TimeAdjustmentState.TIME_ENDED, viewData)
    }

    override fun onBackPressed() {
        if (chooserState.value?.current !is ChangeRecordChooserState.State.Closed) {
            onNewChooserState(ChangeRecordChooserState.State.Closed)
        } else {
            delegateScope.launch {
                // Send only if not changed and update only time.
                if (recordState.newTimeStarted == recordState.originalTimeStarted) {
                    mode.sendPreviewUpdate(false)
                }
                router.back()
            }
        }
    }

    override fun showMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }

    private suspend fun openTagSelectionIfNeeded() {
        // If type has any record tags - open tag selection
        val tags = recordTagInteractor.getAll().associateBy { it.id }
        val tagsForThisType = recordTypeToTagInteractor.getTags(recordState.newTypeId)
            .mapNotNull(tags::get)
            .filterNot { it.archived }

        if (tagsForThisType.isNotEmpty()) {
            delay(300)
            onCategoryChooserClick()
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
        onTimeClick(tag = TIME_SPLIT_TAG, timestamp = recordState.newTimeSplit)
    }

    private fun onAdjustTimeSplitItemClick(viewData: TimeAdjustmentView.ViewData) {
        when (viewData) {
            is TimeAdjustmentView.ViewData.Now -> {
                recordState.newTimeSplit = System.currentTimeMillis()
                onTimeSplitChanged()
            }
            is TimeAdjustmentView.ViewData.Zero -> {
                // Do nothing, shouldn't be there.
            }
            is TimeAdjustmentView.ViewData.Adjust -> {
                recordState.newTimeSplit += TimeUnit.MINUTES.toMillis(viewData.value)
                onTimeSplitChanged()
            }
        }
    }

    private fun onSliderSplitValueChanged(value: Float) {
        recordState.newTimeSplit = recordState.newTimeStarted + TimeUnit.SECONDS.toMillis(value.toLong())
        onTimeSplitChanged()
    }

    private fun onRecordChangeButtonClick(
        onProceed: suspend () -> Unit,
        checkTypeSelected: Boolean = true,
        checkSplitTypeSelected: Boolean = false,
        delayBlock: Boolean = false,
    ) {
        if (checkTypeSelected && recordState.newTypeId == 0L) {
            showMessage(R.string.change_record_message_choose_type)
            return
        }
        if (checkSplitTypeSelected &&
            (recordState.newSplitBeforeTypeId ?: recordState.newTypeId) == 0L
        ) {
            showMessage(R.string.change_record_message_choose_type)
            return
        }
        delegateScope.launch {
            val canProceed = saveButtonEnabled.value.orFalse()
            if (!canProceed) return@launch
            if (!delayBlock) {
                saveButtonEnabled.set(false)
                updateActionsData()
            }
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
        val showKeyboard = newState is ChangeRecordChooserState.State.Comment
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
    ) = delegateScope.launch {
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
    ) = delegateScope.launch {
        val showSeconds = prefsInteractor.getShowSeconds()

        router.navigate(
            DurationDialogParams(
                tag = tag,
                value = DurationDialogParams.Value.DurationSeconds(
                    duration = durationMillis / 1000,
                ),
                hideDisableButton = true,
                showSeconds = mode.config.forceSecondsInDurationDialog || showSeconds,
            ),
        )
    }

    private fun onTimeStateClick(
        field: ChangeRecordDateTimeField,
    ) = delegateScope.launch {
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

        mode.updatePreview()
    }

    private fun onAdjustTimeItemClick(
        state: TimeAdjustmentState,
        viewData: TimeAdjustmentView.ViewData,
    ) {
        delegateScope.launch {
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
                recordState.newTimeStarted = System.currentTimeMillis()
                mode.onTimeStartedChanged()
            }
            TimeAdjustmentState.TIME_ENDED -> {
                recordState.newTimeEnded = System.currentTimeMillis()
                mode.onTimeEndedChanged()
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
                recordState.newTimeStarted = mode.previewTimeEnded()
                mode.onTimeStartedChanged()
            }
            TimeAdjustmentState.TIME_ENDED -> {
                recordState.newTimeEnded = recordState.newTimeStarted
                mode.onTimeEndedChanged()
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
                        recordState.newTimeStarted += shift
                    }
                    is ChangeRecordDateTimeFieldsState.State.Duration -> {
                        recordState.newTimeStarted = (recordState.newTimeStarted - shift)
                            .coerceAtMost(mode.previewTimeEnded())
                    }
                }
                mode.onTimeStartedChanged()
            }
            TimeAdjustmentState.TIME_ENDED -> {
                when (dateTimeState.end) {
                    is ChangeRecordDateTimeFieldsState.State.DateTime -> {
                        recordState.newTimeEnded += shift
                    }
                    is ChangeRecordDateTimeFieldsState.State.Duration -> {
                        recordState.newTimeEnded = (mode.previewTimeEnded() + shift)
                            .coerceAtLeast(recordState.newTimeStarted)
                    }
                }
                mode.onTimeEndedChanged()
            }
            else -> {
                // Do nothing, it's hidden.
            }
        }
    }

    private fun onTimeSplitChanged() {
        recordState.newTimeSplit = recordState.newTimeSplit
            .coerceIn(recordState.newTimeStarted..mode.previewTimeEnded())
        updateActionsData()
    }

    private fun openSplitTypeSelection(tag: String) {
        TypesSelectionDialogParams(
            tag = tag,
            title = resourceRepo.getString(R.string.change_record_message_choose_type),
            subtitle = "",
            type = TypesSelectionDialogParams.Type.Activity,
            selectedTypeIds = emptyList(),
            selectedTagValues = emptyList(),
            selectedTagValueOnStart = emptyList(),
            isMultiSelectAvailable = false,
            idsShouldBeVisible = emptyList(),
            showHints = false,
            allowTagValueSelection = false,
        ).let(router::navigate)
    }

    private fun onMainTypeSelected(typeId: Long) {
        if (typeId == recordState.newTypeId) return
        recordState.newTypeId = typeId
        recordState.newTags = emptyList()
        delegateScope.launch {
            mode.updatePreview()
            updateCategoriesViewData()
        }
        commentSelectionViewModelDelegate.updateCommentsViewData()
        updateActionsData()
    }

    private fun getCommentSelectionDelegateParent(): CommentSelectionViewModelDelegate.Parent {
        return object : CommentSelectionViewModelDelegate.Parent {
            override fun getParams(): CommentSelectionViewModelDelegate.Parent.Params =
                CommentSelectionViewModelDelegate.Parent.Params(recordTypeId = recordState.newTypeId)

            override suspend fun onCommentClick() = mode.updatePreview()
            override fun onCommentChange(): Unit = delegateScope.launch { mode.updatePreview() }.let {}
        }
    }

    private fun getDelegateActionsConsumer(): ChangeRecordDelegateBridge.ActionConsumer {
        return object : ChangeRecordDelegateBridge.ActionConsumer {
            override suspend fun onAction(action: Action) {
                when (action) {
                    is Action.UpdateViewData -> {
                        changeRecordActionsDelegate.updateViewData()
                    }
                    is Action.OnSaveClickDelegate -> {
                        mode.onSaveClickDelegate(action.doAfter)
                    }
                    is Action.ShowMessage -> {
                        this@ChangeRecordEditorDelegateImpl.showMessage(action.messageResId)
                    }
                    is Action.OnSplitComplete -> {
                        recordState.newTimeStarted = recordState.newTimeSplit
                        mode.onSaveClickDelegate {}
                    }
                    is Action.OnRecordChangeButtonClick -> {
                        this@ChangeRecordEditorDelegateImpl.onRecordChangeButtonClick(
                            onProceed = action.onProceed,
                            checkTypeSelected = action.checkTypeSelected,
                            checkSplitTypeSelected = action.checkSplitTypeSelected,
                            delayBlock = action.delayBlock,
                        )
                    }
                }
            }
        }
    }

    private fun getDelegateParamsProvider(): ChangeRecordDelegateBridge.ParamsProvider {
        return object : ChangeRecordDelegateBridge.ParamsProvider {
            override fun getParams(): ViewDataParams {
                return ViewDataParams(
                    baseParams = ViewDataParams.BaseParams(
                        newTypeId = recordState.newTypeId,
                        newTimeStarted = recordState.newTimeStarted,
                        newTimeEnded = recordState.newTimeEnded,
                        newComment = commentSelectionViewModelDelegate.newComment,
                        newTags = recordState.newTags,
                        isButtonEnabled = saveButtonEnabled.value.orFalse(),
                    ),
                    splitParams = ViewDataParams.SplitParams(
                        newTimeSplit = recordState.newTimeSplit,
                        newBeforeTypeId = recordState.newSplitBeforeTypeId ?: recordState.newTypeId,
                        splitPreviewTimeEnded = mode.previewTimeEnded(),
                        showTimeEndedOnSplitPreview = mode.config.showTimeEndedOnSplitPreview,
                        originalTypeId = recordState.originalTypeId,
                        originalTags = recordState.originalTags,
                    ),
                    duplicateParams = ViewDataParams.DuplicateParams(
                        isAvailable = mode.config.isDuplicateActionAvailable,
                        newTimeEnded = mode.previewTimeEnded(),
                    ),
                    moveParams = ViewDataParams.MoveParams(
                        isAvailable = mode.config.isAdditionalActionsAvailable,
                    ),
                    continueParams = ViewDataParams.ContinueParams(
                        originalRecordId = recordState.originalRecordId,
                        isAvailable = mode.config.isAdditionalActionsAvailable,
                    ),
                    repeatParams = ViewDataParams.RepeatParams(
                        isAvailable = mode.config.isAdditionalActionsAvailable,
                    ),
                    adjustParams = ViewDataParams.AdjustParams(
                        originalRecordId = recordState.originalRecordId,
                        originalTypeId = recordState.originalTypeId,
                        originalTimeStarted = recordState.originalTimeStarted,
                        adjustNextRecordAvailable = mode.config.adjustNextRecordAvailable,
                        adjustPreviewTimeEnded = mode.adjustPreviewTimeEnded(),
                        adjustPreviewOriginalTimeEnded = mode.adjustPreviewOriginalTimeEnded(),
                        showTimeEndedOnAdjustPreview = mode.config.showTimeEndedOnAdjustPreview,
                        isTimeEndedAvailable = mode.config.isTimeEndedAvailable,
                    ),
                    mergeParams = ViewDataParams.MergeParams(
                        mergeAvailable = mode.mergeAvailable(),
                        prevRecord = prevRecord,
                    ),
                    shortcutParams = ViewDataParams.ShortcutParams(
                        isAvailable = true,
                    ),
                )
            }
        }
    }

    private suspend fun initializeActions() {
        initializePrevRecord()
        updateActionsData()
    }

    private suspend fun initializePrevRecord() {
        prevRecord = recordInteractor.getPrev(timeStarted = recordState.originalTimeStarted)
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return recordTypesViewDataInteractor.getTypesViewData()
    }

    suspend fun updateCategoriesViewData(
        fromValueChange: Boolean = false,
    ) {
        val data = loadCategoriesViewData(fromValueChange)
        categories.set(data)
    }

    private suspend fun loadCategoriesViewData(
        fromSearchChange: Boolean,
    ): ChangeRecordTagsViewData {
        return recordTagViewDataInteractor.getViewData(
            selectedTags = recordState.newTags,
            typeIds = listOf(recordState.newTypeId),
            multipleChoiceAvailable = true,
            showBigEmptyHint = true,
            showHint = true,
            showArchived = false,
            searchText = tagSearchText,
            fromSearchChange = fromSearchChange,
            buttons = listOfNotNull(
                RecordTagViewDataInteractor.Button.ADD,
                RecordTagViewDataInteractor.Button.ALL_TAGS,
                RecordTagViewDataInteractor.Button.SEARCH,
                RecordTagViewDataInteractor.Button.UNTAGGED.takeIf { recordState.newTags.isNotEmpty() },
            ),
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

    companion object {
        private const val TIME_STARTED_TAG = "time_started_tag"
        private const val TIME_ENDED_TAG = "time_ended_tag"
        private const val TIME_SPLIT_TAG = "time_split_tag"
        private const val CHANGE_RECORD_TAG_VALUE_SELECTION = "CHANGE_RECORD_TAG_VALUE_SELECTION"
        private const val SPLIT_BEFORE_TYPE_SELECTION = "SPLIT_BEFORE_TYPE_SELECTION"
        private const val SPLIT_AFTER_TYPE_SELECTION = "SPLIT_AFTER_TYPE_SELECTION"
    }
}