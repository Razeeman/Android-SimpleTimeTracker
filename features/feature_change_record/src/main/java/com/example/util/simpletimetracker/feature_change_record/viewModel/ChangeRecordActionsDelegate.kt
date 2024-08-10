package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

interface ChangeRecordActionsDelegate {
    val actionsViewData: LiveData<List<ViewHolderType>>

    interface Parent {
        fun getViewDataParams(): ViewDataParams

        fun updateViewData()

        fun onRecordChangeButtonClick(
            onProceed: suspend () -> Unit,
            checkTypeSelected: Boolean = true,
        )

        suspend fun onSaveClickDelegate()

        suspend fun onSplitComplete()

        fun showMessage(stringResId: Int)

        data class ViewDataParams(
            val baseParams: BaseParams,
            val splitParams: SplitParams,
            val duplicateParams: DuplicateParams,
            val continueParams: ContinueParams,
            val repeatParams: RepeatParams,
            val adjustParams: AdjustParams,
            val mergeParams: MergeParams,
        ) {

            data class BaseParams(
                val newTypeId: Long,
                val newTimeStarted: Long,
                val newTimeEnded: Long,
                val newComment: String,
                val newCategoryIds: List<Long>,
                val isButtonEnabled: Boolean,
            )

            data class SplitParams(
                val newTimeSplit: Long,
                val splitPreviewTimeEnded: Long,
                val showTimeEndedOnSplitPreview: Boolean,
            )

            data class DuplicateParams(
                val isAdditionalActionsAvailable: Boolean,
            )

            data class ContinueParams(
                val originalRecordId: Long,
                val isAdditionalActionsAvailable: Boolean,
            )

            data class RepeatParams(
                val isAdditionalActionsAvailable: Boolean,
            )

            data class AdjustParams(
                val originalRecordId: Long,
                val originalTypeId: Long,
                val originalTimeStarted: Long,
                val adjustNextRecordAvailable: Boolean,
                val adjustPreviewTimeEnded: Long,
                val adjustPreviewOriginalTimeEnded: Long,
                val showTimeEndedOnAdjustPreview: Boolean,
                val isTimeEndedAvailable: Boolean,
            )

            data class MergeParams(
                val mergeAvailable: Boolean,
                val prevRecord: Record?,
            )
        }
    }
}