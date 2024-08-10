package com.example.util.simpletimetracker.feature_change_record.mapper

import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordActionsAdjustDelegate
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordActionsContinueDelegate
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordActionsDelegate
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordActionsDelegate.Parent.ViewDataParams
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordActionsDuplicateDelegate
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordActionsMergeDelegate
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordActionsRepeatDelegate
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordActionsSplitDelegate
import javax.inject.Inject

class ChangeRecordActionsDelegateMapper @Inject constructor() {

    fun getSplitDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent?,
        updateViewData: () -> Unit,
    ): ChangeRecordActionsSplitDelegate.Parent {
        return object : ChangeRecordActionsSplitDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsSplitDelegate.Parent.ViewDataParams? {
                return parent?.getViewDataParams()?.mapSplitParams()
            }

            override fun update() {
                updateViewData()
            }

            override suspend fun onSplitComplete() {
                parent?.onSplitComplete()
            }
        }
    }

    fun getAdjustDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent?,
        updateViewData: () -> Unit,
    ): ChangeRecordActionsAdjustDelegate.Parent {
        return object : ChangeRecordActionsAdjustDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsAdjustDelegate.Parent.ViewDataParams? {
                return parent?.getViewDataParams()?.mapAdjustParams()
            }

            override fun update() {
                updateViewData()
            }

            override suspend fun onAdjustComplete() {
                parent?.onSaveClickDelegate()
            }
        }
    }

    fun getContinueActionsDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent?,
        updateViewData: () -> Unit,
    ): ChangeRecordActionsContinueDelegate.Parent {
        return object : ChangeRecordActionsContinueDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsContinueDelegate.Parent.ViewDataParams? {
                return parent?.getViewDataParams()?.mapContinueParams()
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

    fun getRepeatActionsDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent?,
        updateViewData: () -> Unit,
    ): ChangeRecordActionsRepeatDelegate.Parent {
        return object : ChangeRecordActionsRepeatDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsRepeatDelegate.Parent.ViewDataParams? {
                return parent?.getViewDataParams()?.mapRepeatParams()
            }

            override fun update() {
                updateViewData()
            }

            override suspend fun onSaveClickDelegate() {
                parent?.onSaveClickDelegate()
            }
        }
    }

    fun getDuplicateActionsDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent?,
        updateViewData: () -> Unit,
    ): ChangeRecordActionsDuplicateDelegate.Parent {
        return object : ChangeRecordActionsDuplicateDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsDuplicateDelegate.Parent.ViewDataParams? {
                return parent?.getViewDataParams()?.mapDuplicateParams()
            }

            override fun update() {
                updateViewData()
            }

            override suspend fun onSaveClickDelegate() {
                parent?.onSaveClickDelegate()
            }
        }
    }

    fun getMergeDelegateParent(
        parent: ChangeRecordActionsDelegate.Parent?,
        updateViewData: () -> Unit,
    ): ChangeRecordActionsMergeDelegate.Parent {
        return object : ChangeRecordActionsMergeDelegate.Parent {
            override fun getViewDataParams(): ChangeRecordActionsMergeDelegate.Parent.ViewDataParams? {
                return parent?.getViewDataParams()?.mapMergeParams()
            }

            override fun update() {
                updateViewData()
            }
        }
    }

    private fun ViewDataParams.mapSplitParams(): ChangeRecordActionsSplitDelegate.Parent.ViewDataParams {
        return ChangeRecordActionsSplitDelegate.Parent.ViewDataParams(
            newTimeSplit = splitParams.newTimeSplit,
            newTypeId = baseParams.newTypeId,
            newTimeStarted = baseParams.newTimeStarted,
            splitPreviewTimeEnded = splitParams.splitPreviewTimeEnded,
            newComment = baseParams.newComment,
            newCategoryIds = baseParams.newCategoryIds,
            showTimeEndedOnSplitPreview = splitParams.showTimeEndedOnSplitPreview,
            isButtonEnabled = baseParams.isButtonEnabled,
        )
    }

    private fun ViewDataParams.mapAdjustParams(): ChangeRecordActionsAdjustDelegate.Parent.ViewDataParams {
        return ChangeRecordActionsAdjustDelegate.Parent.ViewDataParams(
            originalRecordId = adjustParams.originalRecordId,
            adjustNextRecordAvailable = adjustParams.adjustNextRecordAvailable,
            newTypeId = baseParams.newTypeId,
            newTimeStarted = baseParams.newTimeStarted,
            newTimeEnded = baseParams.newTimeEnded,
            adjustPreviewTimeEnded = adjustParams.adjustPreviewTimeEnded,
            originalTypeId = adjustParams.originalTypeId,
            originalTimeStarted = adjustParams.originalTimeStarted,
            adjustPreviewOriginalTimeEnded = adjustParams.adjustPreviewOriginalTimeEnded,
            showTimeEndedOnAdjustPreview = adjustParams.showTimeEndedOnAdjustPreview,
            isTimeEndedAvailable = adjustParams.isTimeEndedAvailable,
            isButtonEnabled = baseParams.isButtonEnabled,
        )
    }

    private fun ViewDataParams.mapContinueParams(): ChangeRecordActionsContinueDelegate.Parent.ViewDataParams {
        return ChangeRecordActionsContinueDelegate.Parent.ViewDataParams(
            originalRecordId = continueParams.originalRecordId,
            newTypeId = baseParams.newTypeId,
            newTimeStarted = baseParams.newTimeStarted,
            newComment = baseParams.newComment,
            newCategoryIds = baseParams.newCategoryIds,
            isAdditionalActionsAvailable = continueParams.isAdditionalActionsAvailable,
            isButtonEnabled = baseParams.isButtonEnabled,
        )
    }

    private fun ViewDataParams.mapRepeatParams(): ChangeRecordActionsRepeatDelegate.Parent.ViewDataParams {
        return ChangeRecordActionsRepeatDelegate.Parent.ViewDataParams(
            newTypeId = baseParams.newTypeId,
            newComment = baseParams.newComment,
            newCategoryIds = baseParams.newCategoryIds,
            isAdditionalActionsAvailable = repeatParams.isAdditionalActionsAvailable,
            isButtonEnabled = baseParams.isButtonEnabled,
        )
    }

    private fun ViewDataParams.mapDuplicateParams(): ChangeRecordActionsDuplicateDelegate.Parent.ViewDataParams {
        return ChangeRecordActionsDuplicateDelegate.Parent.ViewDataParams(
            newTypeId = baseParams.newTypeId,
            newTimeStarted = baseParams.newTimeStarted,
            newTimeEnded = baseParams.newTimeEnded,
            newComment = baseParams.newComment,
            newCategoryIds = baseParams.newCategoryIds,
            isAdditionalActionsAvailable = duplicateParams.isAdditionalActionsAvailable,
            isButtonEnabled = baseParams.isButtonEnabled,
        )
    }

    private fun ViewDataParams.mapMergeParams(): ChangeRecordActionsMergeDelegate.Parent.ViewDataParams {
        return ChangeRecordActionsMergeDelegate.Parent.ViewDataParams(
            mergeAvailable = mergeParams.mergeAvailable,
            prevRecord = mergeParams.prevRecord,
            newTimeEnded = baseParams.newTimeEnded,
            isButtonEnabled = baseParams.isButtonEnabled,
        )
    }
}