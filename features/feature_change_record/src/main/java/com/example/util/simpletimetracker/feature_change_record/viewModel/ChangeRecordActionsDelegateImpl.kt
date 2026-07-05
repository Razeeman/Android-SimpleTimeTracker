package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeDoublePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordQuickActionsButtonViewData
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordActionsDelegateHolder
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordDelegateBridge
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordActionsDelegateImpl @Inject constructor(
    private val delegateHolder: ChangeRecordActionsDelegateHolder,
) : ViewModelDelegate() {

    val actionsViewData: LiveData<List<ViewHolderType>> by lazySuspend { loadViewData() }

    val timeChangeAdjustmentState get() = delegateHolder.adjustDelegate.timeChangeAdjustmentState

    private var bridge: ChangeRecordDelegateBridge? = null
    private var updateJob: Job? = null

    fun attach(bridge: ChangeRecordDelegateBridge) {
        this.bridge = bridge
        delegateHolder.attach(bridge)
    }

    override fun clear() {
        delegateHolder.clear()
        super.clear()
    }

    fun updateData() {
        updateJob?.cancel()
        updateJob = delegateScope.launch {
            delegateHolder.updateViewData()
        }
    }

    fun updateViewData() {
        val data = loadViewData()
        actionsViewData.set(data)
    }

    fun onItemAdjustTimeStartedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        when (data.block) {
            ChangeRecordActionsBlock.AdjustTimePreview ->
                delegateHolder.adjustDelegate.onAdjustTimeStartedClick()
            else -> {
                // Do nothing.
            }
        }
    }

    fun onItemAdjustTimeEndedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        when (data.block) {
            ChangeRecordActionsBlock.AdjustTimePreview ->
                delegateHolder.adjustDelegate.onAdjustTimeEndedClick()
            else -> {
                // Do nothing.
            }
        }
    }

    fun onChangePreviewCheckClick(item: ChangeRecordChangePreviewViewData) {
        delegateHolder.adjustDelegate.onChangePreviewCheckClick(item)
    }

    fun onItemButtonClick(viewData: ButtonViewData) = delegateScope.launch {
        val id = viewData.id as? ChangeRecordQuickActionsButtonViewData ?: return@launch
        when (id.block) {
            ChangeRecordActionsBlock.SplitButton -> onSplitClick()
            ChangeRecordActionsBlock.AdjustButton -> onAdjustClick()
            ChangeRecordActionsBlock.ContinueButton -> onContinueClick()
            ChangeRecordActionsBlock.RepeatButton -> onRepeatClick()
            ChangeRecordActionsBlock.DuplicateButton -> onDuplicateClick()
            ChangeRecordActionsBlock.MoveButton -> onMoveClick()
            ChangeRecordActionsBlock.MergeButton -> onMergeClick()
            ChangeRecordActionsBlock.ShortcutButton -> onShortcutClick()
            else -> {
                // Do nothing.
            }
        }
    }

    private suspend fun onAdjustClick() {
        onRecordChangeButtonClick(
            onProceed = delegateHolder.adjustDelegate::onAdjustClickDelegate,
        )
    }

    private suspend fun onContinueClick() {
        if (!delegateHolder.continueDelegate.canContinue()) return
        onRecordChangeButtonClick(
            onProceed = delegateHolder.continueDelegate::onContinueClickDelegate,
        )
    }

    private suspend fun onRepeatClick() {
        onRecordChangeButtonClick(
            onProceed = delegateHolder.repeatDelegate::onRepeatClickDelegate,
        )
    }

    private suspend fun onDuplicateClick() {
        onRecordChangeButtonClick(
            onProceed = delegateHolder.duplicateDelegate::onDuplicateClickDelegate,
        )
    }

    private suspend fun onMoveClick() {
        onRecordChangeButtonClick(
            onProceed = delegateHolder.moveDelegate::onMoveClickDelegate,
            delayBlock = true,
        )
    }

    private suspend fun onMergeClick() {
        onRecordChangeButtonClick(
            onProceed = delegateHolder.mergeDelegate::onMergeClickDelegate,
            checkTypeSelected = false,
        )
    }

    private suspend fun onShortcutClick() {
        onRecordChangeButtonClick(
            onProceed = delegateHolder.shortcutDelegate::onShortcutClickDelegate,
        )
    }

    private suspend fun onSplitClick() {
        onRecordChangeButtonClick(
            onProceed = delegateHolder.splitDelegate::onSplitClickDelegate,
            checkSplitTypeSelected = true,
        )
    }

    private suspend fun onRecordChangeButtonClick(
        onProceed: suspend () -> Unit,
        checkTypeSelected: Boolean = true,
        checkSplitTypeSelected: Boolean = false,
        delayBlock: Boolean = false,
    ) {
        bridge?.send(
            ChangeRecordDelegateBridge.Action.OnRecordChangeButtonClick(
                onProceed = onProceed,
                checkTypeSelected = checkTypeSelected,
                checkSplitTypeSelected = checkSplitTypeSelected,
                delayBlock = delayBlock,
            ),
        )
    }

    private fun loadViewData(): List<ViewHolderType> {
        return delegateHolder.loadViewData()
    }
}