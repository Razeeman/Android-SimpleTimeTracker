package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.interactor.RecordActionContinueMediator
import com.example.util.simpletimetracker.domain.interactor.RecordActionDuplicateMediator
import com.example.util.simpletimetracker.domain.interactor.RecordActionMergeMediator
import com.example.util.simpletimetracker.domain.interactor.RecordActionRepeatMediator
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams.Type
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordQuickActionsViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val recordInteractor: RecordInteractor,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val recordActionDuplicateMediator: RecordActionDuplicateMediator,
    private val recordActionRepeatMediator: RecordActionRepeatMediator,
    private val recordActionContinueMediator: RecordActionContinueMediator,
    private val recordActionMergeMediator: RecordActionMergeMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
) : BaseViewModel() {

    lateinit var extra: RecordQuickActionsParams

    val state: LiveData<RecordQuickActionsState> by lazySuspend { loadState() }
    val buttonsEnabled: LiveData<Boolean> = MutableLiveData(true)
    val requestUpdate: LiveData<Unit> = SingleLiveEvent<Unit>()

    fun onStatisticsClicked() {
        onButtonClick(onProceed = ::goToStatistics)
    }

    fun onDeleteClicked() {
        onButtonClick(onProceed = ::onDelete)
    }

    fun onContinueClicked() {
        onButtonClick(canProceed = ::canContinue, onProceed = ::onContinue)
    }

    fun onRepeatClicked() {
        onButtonClick(onProceed = ::onRepeat)
    }

    fun onDuplicateClicked() {
        onButtonClick(onProceed = ::onDuplicate)
    }

    fun onMergeClicked() {
        onButtonClick(onProceed = ::onMerge)
    }

    private suspend fun goToStatistics() {
        val params = extra.type ?: return
        val preview = extra.preview ?: return
        val itemId = when (params) {
            is Type.RecordTracked -> recordInteractor.get(params.id)?.typeId ?: return
            is Type.RecordUntracked -> UNTRACKED_ITEM_ID
            is Type.RecordRunning -> params.id
        }

        statisticsDetailNavigationInteractor.navigate(
            transitionName = "",
            filterType = ChartFilterType.ACTIVITY,
            shift = 0,
            sharedElements = emptyMap(),
            itemId = itemId,
            itemName = preview.name,
            itemIcon = preview.iconId.toViewData(),
            itemColor = preview.color,
        )
    }

    private suspend fun onDelete() {
        when (val params = extra.type) {
            is Type.RecordTracked -> {
                // Removal handled in separate viewModel.
                router.back()
            }
            is Type.RecordUntracked -> {
                // Do nothing, shouldn't be possible.
            }
            is Type.RecordRunning -> {
                removeRunningRecordMediator.remove(params.id, updateWidgets = true)
                showMessage(R.string.change_running_record_removed)
                router.back()
            }
            null -> {
                // Do nothing, something went wrong.
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
            tagIds = record.tagIds,
        )
        exit()
    }

    private suspend fun onRepeat() {
        val record = getTrackedRecord() ?: return
        recordActionRepeatMediator.execute(
            typeId = record.typeId,
            comment = record.comment,
            tagIds = record.tagIds,
        )
        exit()
    }

    private suspend fun onDuplicate() {
        val record = getTrackedRecord() ?: return
        recordActionDuplicateMediator.execute(
            typeId = record.typeId,
            timeStarted = record.timeStarted,
            timeEnded = record.timeEnded,
            comment = record.comment,
            tagIds = record.tagIds,
        )
        exit()
    }

    private suspend fun onMerge() {
        val record = extra.type as? Type.RecordUntracked
            ?: return
        val prevRecord = recordInteractor.getPrev(timeStarted = record.timeStarted).firstOrNull()
        recordActionMergeMediator.execute(
            prevRecord = prevRecord,
            newTimeEnded = record.timeEnded,
            onMergeComplete = ::exit,
        )
    }

    private suspend fun getTrackedRecord(): Record? {
        val recordId = (extra.type as? Type.RecordTracked)?.id
            ?: return null
        return recordInteractor.get(recordId)
    }

    private fun onButtonClick(
        canProceed: suspend () -> Boolean = { true },
        onProceed: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            if (!canProceed()) return@launch
            buttonsEnabled.set(false)
            onProceed()
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
        requestUpdate.set(Unit)
        router.back()
    }

    private fun loadState(): RecordQuickActionsState {
        val buttons = when (extra.type) {
            is Type.RecordTracked -> listOf(
                RecordQuickActionsState.Button.Statistics(false),
                RecordQuickActionsState.Button.Delete(false),
                RecordQuickActionsState.Button.Continue(false),
                RecordQuickActionsState.Button.Repeat(false),
                RecordQuickActionsState.Button.Duplicate(true),
            )
            is Type.RecordUntracked -> listOf(
                RecordQuickActionsState.Button.Statistics(false),
                RecordQuickActionsState.Button.Merge(true),
            )
            is Type.RecordRunning -> listOf(
                RecordQuickActionsState.Button.Statistics(false),
                RecordQuickActionsState.Button.Delete(false),
            )
            null -> emptyList()
        }

        return RecordQuickActionsState(
            buttons = buttons,
        )
    }
}
