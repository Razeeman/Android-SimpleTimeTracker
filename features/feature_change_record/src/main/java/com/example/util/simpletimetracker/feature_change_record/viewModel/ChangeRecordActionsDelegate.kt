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
import javax.inject.Inject

interface ChangeRecordActionsDelegate {
    val actionsViewData: LiveData<List<ViewHolderType>>
}

class ChangeRecordActionsDelegateImpl @Inject constructor(
    private val mergeDelegate: ChangeRecordActionsMergeDelegate,
    private val splitDelegate: ChangeRecordActionsSplitDelegate,
    private val adjustDelegate: ChangeRecordActionsAdjustDelegate,
    private val additionalDelegate: ChangeRecordActionsAdditionalDelegate,
) : ChangeRecordActionsDelegate, ViewModelDelegate() {

    override val actionsViewData: LiveData<List<ViewHolderType>> by lazySuspend { loadViewData() }

    val timeChangeAdjustmentState get() = adjustDelegate.timeChangeAdjustmentState

    private var parent: Parent? = null

    init {
        splitDelegate.attach(getSplitDelegateParent())
        adjustDelegate.attach(getAdjustDelegateParent())
        additionalDelegate.attach(getAdditionalActionsDelegateParent())
        mergeDelegate.attach(getMergeDelegateParent())
    }

    fun attach(parent: Parent) {
        this.parent = parent
    }

    override fun clear() {
        adjustDelegate.clear()
        super.clear()
    }

    suspend fun updateViewData() {
        val data = loadViewData()
        actionsViewData.set(data)
    }

    fun onItemAdjustTimeStartedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        when (data.block) {
            ChangeRecordActionsBlock.AdjustTimePreview ->
                adjustDelegate.onAdjustTimeStartedClick()
            else -> {
                // Do nothing.
            }
        }
    }

    fun onItemAdjustTimeEndedClick(data: ChangeRecordTimeDoublePreviewViewData) {
        when (data.block) {
            ChangeRecordActionsBlock.AdjustTimePreview ->
                adjustDelegate.onAdjustTimeEndedClick()
            else -> {
                // Do nothing.
            }
        }
    }

    fun onChangePreviewCheckClick(item: ChangeRecordChangePreviewViewData) {
        adjustDelegate.onChangePreviewCheckClick(item)
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
            onProceed = adjustDelegate::onAdjustClickDelegate,
        )
    }

    private fun onContinueClick() {
        if (!additionalDelegate.canContinue()) return
        parent?.onRecordChangeButtonClick(
            onProceed = additionalDelegate::onContinueClickDelegate,
        )
    }

    private fun onRepeatClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = additionalDelegate::onRepeatClickDelegate,
        )
    }

    private fun onDuplicateClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = additionalDelegate::onDuplicateClickDelegate,
        )
    }

    private fun onMergeClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = mergeDelegate::onMergeClickDelegate,
            checkTypeSelected = false,
        )
    }

    private fun onSplitClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = {
                splitDelegate.onSplitClickDelegate()
            },
        )
    }

    private fun getSplitDelegateParent(): ChangeRecordActionsSplitDelegate.Parent {
        return object : ChangeRecordActionsSplitDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsSplitDelegate.Parent.ViewDataParams? {
                return parent?.getSplitViewDataParams()
            }

            override suspend fun onSplitComplete() {
                parent?.onSplitComplete()
            }
        }
    }

    private fun getAdjustDelegateParent(): ChangeRecordActionsAdjustDelegate.Parent {
        return object : ChangeRecordActionsAdjustDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsAdjustDelegate.Parent.ViewDataParams? {
                return parent?.getAdjustViewDataParams()
            }

            override suspend fun update() {
                updateViewData()
            }

            override suspend fun onAdjustComplete() {
                parent?.onSaveClickDelegate()
            }
        }
    }

    private fun getAdditionalActionsDelegateParent(): ChangeRecordActionsAdditionalDelegate.Parent {
        return object : ChangeRecordActionsAdditionalDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsAdditionalDelegate.Parent.ViewDataParams? {
                return parent?.getAdditionalViewDataParams()
            }

            override suspend fun onSaveClickDelegate() {
                parent?.onSaveClickDelegate()
            }

            override fun showMessage(stringResId: Int) {
                parent?.showMessage(stringResId)
            }
        }
    }

    private fun getMergeDelegateParent(): ChangeRecordActionsMergeDelegate.Parent {
        return object : ChangeRecordActionsMergeDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsMergeDelegate.Parent.ViewDataParams? {
                return parent?.getMergeViewDataParams()
            }
        }
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        listOf(
            splitDelegate.getViewData(),
            adjustDelegate.getViewData(),
            additionalDelegate.getContinueViewData(),
            additionalDelegate.getRepeatViewData(),
            additionalDelegate.getDuplicateViewData(),
            mergeDelegate.getViewData(),
        ).forEachIndexed { index, items ->
            if (items.isEmpty()) return@forEachIndexed
            if (index != 0) result += DividerViewData(index.toLong())
            result += items
        }

        return result
    }

    interface Parent {
        fun getSplitViewDataParams(): ChangeRecordActionsSplitDelegate.Parent.ViewDataParams
        fun getAdjustViewDataParams(): ChangeRecordActionsAdjustDelegate.Parent.ViewDataParams
        fun getAdditionalViewDataParams(): ChangeRecordActionsAdditionalDelegate.Parent.ViewDataParams?
        fun getMergeViewDataParams(): ChangeRecordActionsMergeDelegate.Parent.ViewDataParams

        fun onRecordChangeButtonClick(
            onProceed: suspend () -> Unit,
            checkTypeSelected: Boolean = true,
        )

        suspend fun onSaveClickDelegate()

        suspend fun onSplitComplete()

        fun showMessage(stringResId: Int)
    }
}