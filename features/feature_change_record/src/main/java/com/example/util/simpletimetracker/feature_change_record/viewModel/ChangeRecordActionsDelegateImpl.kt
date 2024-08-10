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
    private val mergeDelegate: ChangeRecordActionsMergeDelegate,
    private val splitDelegate: ChangeRecordActionsSplitDelegate,
    private val adjustDelegate: ChangeRecordActionsAdjustDelegate,
    private val continueDelegate: ChangeRecordActionsContinueDelegate,
    private val repeatDelegate: ChangeRecordActionsRepeatDelegate,
    private val duplicateDelegate: ChangeRecordActionsDuplicateDelegate,
) : ChangeRecordActionsDelegateBase, ViewModelDelegate() {

    override val actionsViewData: LiveData<List<ViewHolderType>> by lazySuspend { loadViewData() }

    val timeChangeAdjustmentState get() = adjustDelegate.timeChangeAdjustmentState

    private var parent: ChangeRecordActionsDelegateBase.Parent? = null
    private val delegatesList = listOf(
        splitDelegate,
        adjustDelegate,
        continueDelegate,
        repeatDelegate,
        duplicateDelegate,
        mergeDelegate,
    )

    init {
        splitDelegate.attach(getSplitDelegateParent())
        adjustDelegate.attach(getAdjustDelegateParent())
        continueDelegate.attach(getContinueActionsDelegateParent())
        repeatDelegate.attach(getRepeatActionsDelegateParent())
        duplicateDelegate.attach(getDuplicateActionsDelegateParent())
        mergeDelegate.attach(getMergeDelegateParent())
    }

    fun attach(parent: ChangeRecordActionsDelegateBase.Parent) {
        this.parent = parent
    }

    override fun clear() {
        adjustDelegate.clear()
        super.clear()
    }

    fun updateData() {
        delegatesList.forEach { delegate ->
            delegateScope.launch { delegate.updateViewData() }
        }
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
        if (!continueDelegate.canContinue()) return
        parent?.onRecordChangeButtonClick(
            onProceed = continueDelegate::onContinueClickDelegate,
        )
    }

    private fun onRepeatClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = repeatDelegate::onRepeatClickDelegate,
        )
    }

    private fun onDuplicateClick() {
        parent?.onRecordChangeButtonClick(
            onProceed = duplicateDelegate::onDuplicateClickDelegate,
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

    private fun updateViewData() {
        val data = loadViewData()
        actionsViewData.set(data)
    }

    private fun loadViewData(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        delegatesList.map {
            it.getViewData()
        }.forEachIndexed { index, items ->
            if (items.isEmpty()) return@forEachIndexed
            if (index != 0) result += DividerViewData(index.toLong())
            result += items
        }

        return result
    }

    private fun getSplitDelegateParent(): ChangeRecordActionsSplitDelegate.Parent {
        return object : ChangeRecordActionsSplitDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsSplitDelegate.Parent.ViewDataParams? {
                return parent?.getSplitViewDataParams()
            }

            override fun update() {
                updateViewData()
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

            override fun update() {
                updateViewData()
            }

            override suspend fun onAdjustComplete() {
                parent?.onSaveClickDelegate()
            }
        }
    }

    private fun getContinueActionsDelegateParent(): ChangeRecordActionsContinueDelegate.Parent {
        return object : ChangeRecordActionsContinueDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsContinueDelegate.Parent.ViewDataParams? {
                return parent?.getContinueViewDataParams()
            }

            override fun update() {
                updateViewData()
            }

            override suspend fun onSaveClickDelegate() {
                parent?.onSaveClickDelegate()
            }

            override fun showMessage(stringResId: Int) {
                parent?.showMessage(stringResId)
            }
        }
    }

    private fun getRepeatActionsDelegateParent(): ChangeRecordActionsRepeatDelegate.Parent {
        return object : ChangeRecordActionsRepeatDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsRepeatDelegate.Parent.ViewDataParams? {
                return parent?.getRepeatViewDataParams()
            }

            override fun update() {
                updateViewData()
            }

            override suspend fun onSaveClickDelegate() {
                parent?.onSaveClickDelegate()
            }
        }
    }

    private fun getDuplicateActionsDelegateParent(): ChangeRecordActionsDuplicateDelegate.Parent {
        return object : ChangeRecordActionsDuplicateDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsDuplicateDelegate.Parent.ViewDataParams? {
                return parent?.getDuplicateViewDataParams()
            }

            override fun update() {
                updateViewData()
            }

            override suspend fun onSaveClickDelegate() {
                parent?.onSaveClickDelegate()
            }
        }
    }

    private fun getMergeDelegateParent(): ChangeRecordActionsMergeDelegate.Parent {
        return object : ChangeRecordActionsMergeDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsMergeDelegate.Parent.ViewDataParams? {
                return parent?.getMergeViewDataParams()
            }

            override fun update() {
                updateViewData()
            }
        }
    }
}