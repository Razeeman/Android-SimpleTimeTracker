package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsContainerMultiselectInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.MultiSelectedRecordId
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionContinueMediator
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionDuplicateMediator
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionMergeMediator
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionRepeatMediator
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor.RecordQuickActionsInteractor
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor.RecordQuickActionsViewDataInteractor
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsButton
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.HelpDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams.Type
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordQuickActionsViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordInteractor: RecordInteractor,
    private val recordQuickActionsViewDataInteractor: RecordQuickActionsViewDataInteractor,
    private val recordQuickActionsInteractor: RecordQuickActionsInteractor,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val recordActionDuplicateMediator: RecordActionDuplicateMediator,
    private val recordActionRepeatMediator: RecordActionRepeatMediator,
    private val recordActionContinueMediator: RecordActionContinueMediator,
    private val recordActionMergeMediator: RecordActionMergeMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordsContainerMultiselectInteractor: RecordsContainerMultiselectInteractor,
) : BaseViewModel() {

    lateinit var extra: RecordQuickActionsParams

    val state: LiveData<RecordQuickActionsState> by lazySuspend { loadState() }
    val removeRecordIds: LiveData<Set<Long>> = SingleLiveEvent()
    val actionComplete: LiveData<Unit> = SingleLiveEvent<Unit>()

    private var buttonsBlocked: Boolean = false

    fun onButtonClick(block: RecordQuickActionsButton) {
        when (block) {
            RecordQuickActionsButton.DELETE ->
                onButtonClick(onProceed = ::onDelete)
            RecordQuickActionsButton.STATISTICS ->
                onButtonClick(onProceed = ::goToStatistics)
            RecordQuickActionsButton.CONTINUE ->
                onButtonClick(canProceed = ::canContinue, onProceed = ::onContinue)
            RecordQuickActionsButton.REPEAT ->
                onButtonClick(onProceed = ::onRepeat)
            RecordQuickActionsButton.DUPLICATE ->
                onButtonClick(onProceed = ::onDuplicate)
            RecordQuickActionsButton.MOVE ->
                onButtonClick(delayBlock = true, onProceed = ::onMove)
            RecordQuickActionsButton.MERGE ->
                onButtonClick(onProceed = ::onMerge)
            RecordQuickActionsButton.STOP ->
                onButtonClick(onProceed = ::onStop)
            RecordQuickActionsButton.MULTISELECT ->
                onButtonClick(onProceed = ::onMultiselect)
            RecordQuickActionsButton.CHANGE_ACTIVITY ->
                onButtonClick(delayBlock = true, onProceed = ::onChangeActivity)
            RecordQuickActionsButton.CHANGE_TAG ->
                onButtonClick(delayBlock = true, onProceed = ::onChangeTag)
        }
    }

    fun onHintClick() {
        val hintData = state.value?.helpData ?: return
        if (hintData.isEmpty()) return
        val params = HelpDialogParams(
            title = resourceRepo.getString(R.string.change_record_actions_hint),
            text = hintData,
            isFullscreen = false,
        )
        router.navigate(params)
    }

    fun onMultiselectCancelClick() = viewModelScope.launch {
        recordsContainerMultiselectInteractor.disable()
        exit()
    }

    private suspend fun goToStatistics() {
        val params = extra.type
        val preview = extra.preview
        val itemId = when (params) {
            is Type.RecordTracked -> recordInteractor.get(params.id)?.typeId ?: return
            is Type.RecordUntracked -> UNTRACKED_ITEM_ID
            is Type.RecordRunning -> params.id
        }

        statisticsDetailNavigationInteractor.navigate(
            transitionName = "",
            filterType = ChartFilterType.ACTIVITY,
            shift = 0,
            overrideStatisticsRange = null,
            sharedElements = emptyMap(),
            itemId = itemId,
            itemName = preview.name,
            itemIcon = preview.iconId.toViewData(),
            itemColor = preview.color,
        )
    }

    private suspend fun onDelete() {
        if (recordsContainerMultiselectInteractor.isEnabled) {
            val recordIds = recordsContainerMultiselectInteractor.selectedRecordIds
                .filterIsInstance<MultiSelectedRecordId.Tracked>()
                .map { it.id }
                .toSet()
            removeRecordIds.set(recordIds)
            recordsContainerMultiselectInteractor.disable()
            router.back()
            return
        }
        when (val params = extra.type) {
            is Type.RecordTracked -> {
                // Removal handled in separate viewModel.
                val recordId = (params as? Type.RecordTracked)?.id.orZero()
                removeRecordIds.set(setOf(recordId))
                router.back()
            }
            is Type.RecordUntracked -> {
                // Do nothing, shouldn't be possible.
            }
            is Type.RecordRunning -> {
                removeRunningRecordMediator.remove(params.id)
                showMessage(R.string.change_running_record_removed)
                exit()
            }
        }
    }

    private suspend fun canContinue(): Boolean {
        val record = getTrackedRecord() ?: return false
        // Can't continue future record
        return if (record.timeStarted > System.currentTimeMillis()) {
            showMessage(R.string.cannot_be_in_the_future)
            false
        } else {
            true
        }
    }

    private suspend fun onContinue() {
        val record = getTrackedRecord() ?: return
        recordActionContinueMediator.execute(
            recordId = record.id,
            typeId = record.typeId,
            timeStarted = record.timeStarted,
            comment = record.comment,
            tags = record.tags,
        )
        exit()
    }

    private suspend fun onRepeat() {
        val record = getTrackedRecord() ?: return
        recordActionRepeatMediator.execute(
            typeId = record.typeId,
            comment = record.comment,
            tags = record.tags,
        )
        exit()
    }

    private suspend fun onDuplicate() {
        if (recordsContainerMultiselectInteractor.isEnabled) {
            val recordIds = recordsContainerMultiselectInteractor.selectedRecordIds
                .filterIsInstance<MultiSelectedRecordId.Tracked>()
                .map { it.id }
                .toSet()
            val records = recordIds.mapNotNull { recordInteractor.get(it) }
            recordActionDuplicateMediator.execute(records)
            recordsContainerMultiselectInteractor.disable()
            exit()
            return
        }
        val record = getTrackedRecord() ?: return
        recordActionDuplicateMediator.execute(
            typeId = record.typeId,
            timeStarted = record.timeStarted,
            timeEnded = record.timeEnded,
            comment = record.comment,
            tagIds = record.tags,
        )
        exit()
    }

    private suspend fun onMove() {
        val timestamp: Long
        val type: DateTimeDialogType

        if (recordsContainerMultiselectInteractor.isEnabled) {
            val recordIds = recordsContainerMultiselectInteractor.selectedRecordIds
                .filterIsInstance<MultiSelectedRecordId.Tracked>()
            timestamp = recordQuickActionsViewDataInteractor.getRecords(recordIds)
                .minByOrNull { it.timeStarted }
                ?.timeStarted
                ?: return
            type = DateTimeDialogType.DATE
        } else {
            timestamp = getTrackedRecord()?.timeStarted ?: return
            type = DateTimeDialogType.DATETIME()
        }

        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val showSeconds = prefsInteractor.getShowSeconds()

        DateTimeDialogParams(
            tag = RECORD_QUICK_MOVE_TIME_SELECTION_TAG,
            timestamp = timestamp,
            type = type,
            useMilitaryTime = useMilitaryTime,
            firstDayOfWeek = firstDayOfWeek,
            showSeconds = showSeconds,
        ).let(router::navigate)
    }

    private suspend fun onMerge() {
        val record = extra.type as? Type.RecordUntracked ?: return
        val prevRecord = recordInteractor.getPrev(timeStarted = record.timeStarted)
        recordActionMergeMediator.execute(
            prevRecord = prevRecord,
            newTimeEnded = record.timeEnded,
            onMergeComplete = ::exit,
        )
    }

    private suspend fun onStop() {
        val record = extra.type as? Type.RecordRunning ?: return
        runningRecordInteractor.get(record.id)
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
        exit()
    }

    private suspend fun onMultiselect() {
        if (recordsContainerMultiselectInteractor.isEnabled) {
            recordsContainerMultiselectInteractor.disable()
        } else {
            val id = mapMultiselectId()
            recordsContainerMultiselectInteractor.enable(id)
        }
        exit()
    }

    private fun onChangeActivity() {
        TypesSelectionDialogParams(
            tag = RECORD_QUICK_ACTIONS_TYPE_SELECTION_TAG,
            title = resourceRepo.getString(R.string.change_record_message_choose_type),
            subtitle = "",
            type = TypesSelectionDialogParams.Type.Activity,
            selectedTypeIds = emptyList(),
            selectedTagValues = emptyList(),
            isMultiSelectAvailable = false,
            idsShouldBeVisible = emptyList(),
            showHints = false,
            allowTagValueSelection = false,
        ).let(router::navigate)
    }

    private fun onChangeTag() = viewModelScope.launch {
        val typeIds: List<Long>
        val selectedTypeIds: List<Long>
        val selectedTagValues: List<RecordBase.Tag>
        if (recordsContainerMultiselectInteractor.isEnabled) {
            val multiSelectedIds = recordsContainerMultiselectInteractor.selectedRecordIds
            val records = recordQuickActionsViewDataInteractor.getRecords(multiSelectedIds)
            typeIds = records.map { it.typeIds }.flatten().distinct()
            selectedTypeIds = emptyList()
            selectedTagValues = emptyList()
        } else {
            val record = recordQuickActionsViewDataInteractor.getRecord(extra)
            typeIds = listOfNotNull(record?.typeIds?.firstOrNull())
            selectedTypeIds = record?.tags?.map(RecordBase.Tag::tagId).orEmpty()
            selectedTagValues = record?.tags.orEmpty()
        }

        TypesSelectionDialogParams(
            tag = RECORD_QUICK_ACTIONS_TAG_SELECTION_TAG,
            title = resourceRepo.getString(R.string.records_filter_select_tags),
            subtitle = "",
            type = TypesSelectionDialogParams.Type.Tag.ByType(typeIds),
            selectedTypeIds = selectedTypeIds,
            selectedTagValues = selectedTagValues.map {
                TypesSelectionDialogParams.TagData(
                    tagId = it.tagId,
                    numericValue = it.numericValue,
                )
            },
            isMultiSelectAvailable = true,
            idsShouldBeVisible = selectedTypeIds,
            showHints = true,
            allowTagValueSelection = true,
        ).let(router::navigate)
    }

    fun onTypesSelected(
        tag: String?,
        dataIds: List<Long>,
        tagValues: List<RecordBase.Tag>,
    ) = viewModelScope.launch {
        val paramsList = recordQuickActionsViewDataInteractor.getParamsList(extra)
        when (tag) {
            RECORD_QUICK_ACTIONS_TYPE_SELECTION_TAG -> {
                buttonsBlocked = true
                val typeId = dataIds.firstOrNull() ?: return@launch
                recordQuickActionsInteractor.changeType(paramsList, typeId)
                recordsContainerMultiselectInteractor.disable()
                exit()
            }
            RECORD_QUICK_ACTIONS_TAG_SELECTION_TAG -> {
                buttonsBlocked = true
                val tagValuesMap = tagValues.associateBy { it.tagId }
                val newTags = dataIds.map {
                    RecordBase.Tag(
                        tagId = it,
                        numericValue = tagValuesMap[it]?.numericValue,
                    )
                }
                recordQuickActionsInteractor.changeTags(paramsList, newTags)
                recordsContainerMultiselectInteractor.disable()
                exit()
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        val paramsList = recordQuickActionsViewDataInteractor.getParamsList(extra)
        when (tag) {
            RECORD_QUICK_MOVE_TIME_SELECTION_TAG -> {
                buttonsBlocked = true
                recordQuickActionsInteractor.move(
                    params = paramsList,
                    timestamp = timestamp,
                    changeOnlyDate = recordsContainerMultiselectInteractor.isEnabled,
                )
                recordsContainerMultiselectInteractor.disable()
                exit()
            }
        }
    }

    private suspend fun getTrackedRecord(): Record? {
        val recordId = (extra.type as? Type.RecordTracked)?.id
            ?: return null
        return recordInteractor.get(recordId)
    }

    private fun onButtonClick(
        delayBlock: Boolean = false,
        canProceed: suspend () -> Boolean = { true },
        onProceed: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            if (!canProceed()) return@launch
            if (buttonsBlocked) return@launch
            if (!delayBlock) buttonsBlocked = true
            onProceed()
        }
    }

    private fun mapMultiselectId(): MultiSelectedRecordId {
        return when (val extraType = extra.type) {
            is Type.RecordTracked ->
                MultiSelectedRecordId.Tracked(extraType.id)
            is Type.RecordUntracked ->
                MultiSelectedRecordId.Untracked(
                    timeStartedTimestamp = extraType.timeStarted,
                    timeEndedTimestamp = extraType.timeEnded,
                )
            is Type.RecordRunning ->
                MultiSelectedRecordId.Running(extraType.id)
        }
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(
            message = resourceRepo.getString(stringResId),
            duration = SnackBarParams.Duration.Short,
            inDialog = true,
        )
        router.show(params)
    }

    private fun exit() {
        actionComplete.set(Unit)
        router.back()
    }

    private suspend fun loadState(): RecordQuickActionsState {
        return recordQuickActionsViewDataInteractor.getViewData(extra)
    }

    companion object {
        private const val RECORD_QUICK_ACTIONS_TYPE_SELECTION_TAG = "RECORD_QUICK_ACTIONS_TYPE_SELECTION_TAG"
        private const val RECORD_QUICK_ACTIONS_TAG_SELECTION_TAG = "RECORD_QUICK_ACTIONS_TAG_SELECTION_TAG"
        private const val RECORD_QUICK_MOVE_TIME_SELECTION_TAG = "RECORD_QUICK_MOVE_TIME_SELECTION_TAG"
    }
}
