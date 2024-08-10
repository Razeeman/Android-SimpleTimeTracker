package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

interface ChangeRecordActionsDelegateBase {
    val actionsViewData: LiveData<List<ViewHolderType>>

    interface Parent {
        fun getSplitViewDataParams(): ChangeRecordActionsSplitDelegate.Parent.ViewDataParams
        fun getAdjustViewDataParams(): ChangeRecordActionsAdjustDelegate.Parent.ViewDataParams
        fun getContinueViewDataParams(): ChangeRecordActionsContinueDelegate.Parent.ViewDataParams?
        fun getRepeatViewDataParams(): ChangeRecordActionsRepeatDelegate.Parent.ViewDataParams?
        fun getDuplicateViewDataParams(): ChangeRecordActionsDuplicateDelegate.Parent.ViewDataParams?
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