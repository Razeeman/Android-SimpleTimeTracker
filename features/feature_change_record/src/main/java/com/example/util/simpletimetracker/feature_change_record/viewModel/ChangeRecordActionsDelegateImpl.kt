package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordButtonViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeDoublePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordActionsDelegateImpl @Inject constructor(
    private val delegateHolder: ChangeRecordActionsDelegateHolder,
) : ChangeRecordActionsDelegate, ViewModelDelegate() {

    override val actionsViewData: LiveData<List<ViewHolderType>> by lazySuspend { loadViewData() }

    val timeChangeAdjustmentState get() = delegateHolder.adjustDelegate.timeChangeAdjustmentState

    private var parent: ChangeRecordActionsDelegate.Parent? = null

    fun attach(parent: ChangeRecordActionsDelegate.Parent) {
        this.parent = parent
        delegateHolder.attach(parent)
    }

    override fun clear() {
        delegateHolder.delegatesList.forEach {
            (it as? ViewModelDelegate)?.clear()
        }
        super.clear()
    }

    fun updateData() {
        delegateHolder.delegatesList.forEach { delegate ->
            delegateScope.launch { delegate.updateViewData() }
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

    fun onItemButtonClick(viewData: ChangeRecordButtonViewData) {
        when (viewData.block) {
            ChangeRecordActionsBlock.SplitButton -> onSplitClick()
            ChangeRecordActionsBlock.AdjustButton -> onAdjustClick()
            ChangeRecordActionsBlock.ContinueButton -> onContinueClick()
            ChangeRecordActionsBlock.RepeatButton -> onRepeatClick()
            ChangeRecordActionsBlock.DuplicateButton -> onDuplicateClick()
            ChangeRecordActionsBlock.MergeButton -> onMergeClick()
            else -> {
                // Do nothing.
            }
        }
    }

    private fun onAdjustClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = delegateHolder.adjustDelegate::onAdjustClickDelegate,
        )
    }

    private fun onContinueClick() {
        if (!delegateHolder.continueDelegate.canContinue()) return
        parent?.onRecordChangeButtonClick(
            onProceed = delegateHolder.continueDelegate::onContinueClickDelegate,
        )
    }

    private fun onRepeatClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = delegateHolder.repeatDelegate::onRepeatClickDelegate,
        )
    }

    private fun onDuplicateClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = delegateHolder.duplicateDelegate::onDuplicateClickDelegate,
        )
    }

    private fun onMergeClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = delegateHolder.mergeDelegate::onMergeClickDelegate,
            checkTypeSelected = false,
        )
    }

    private fun onSplitClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = {
                delegateHolder.splitDelegate.onSplitClickDelegate()
            },
        )
    }

    private fun loadViewData(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        delegateHolder.delegatesList.map {
            it.getViewData()
        }.forEachIndexed { index, items ->
            if (items.isEmpty()) return@forEachIndexed
            if (index != 0) result += DividerViewData(index.toLong())
            result += items
        }

        return result
    }
}