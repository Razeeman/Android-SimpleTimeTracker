package com.example.util.simpletimetracker.feature_change_record.viewModel.base

import androidx.annotation.StringRes
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase

class ChangeRecordDelegateBridge(
    private val actionConsumer: ActionConsumer,
    private val paramsProvider: ParamsProvider,
) {

    suspend fun send(action: Action) {
        actionConsumer.onAction(action)
    }

    fun getParams(): ViewDataParams {
        return paramsProvider.getParams()
    }

    interface ActionConsumer {
        suspend fun onAction(action: Action)
    }

    interface ParamsProvider {
        fun getParams(): ViewDataParams
    }

    sealed interface Action {
        data object UpdateViewData : Action
        data class OnSaveClickDelegate(val doAfter: suspend () -> Unit = {}) : Action
        data class ShowMessage(@StringRes val messageResId: Int) : Action
        data object OnSplitComplete : Action
        data class OnRecordChangeButtonClick(
            val onProceed: suspend () -> Unit,
            val checkTypeSelected: Boolean = true,
            val checkSplitTypeSelected: Boolean = false,
            val delayBlock: Boolean = false,
        ) : Action
    }

    data class ViewDataParams(
        val baseParams: BaseParams,
        val splitParams: SplitParams,
        val duplicateParams: DuplicateParams,
        val moveParams: MoveParams,
        val continueParams: ContinueParams,
        val repeatParams: RepeatParams,
        val adjustParams: AdjustParams,
        val mergeParams: MergeParams,
        val shortcutParams: ShortcutParams,
    ) {

        data class BaseParams(
            val newTypeId: Long,
            val newTimeStarted: Long,
            val newTimeEnded: Long,
            val newComment: String,
            val newTags: List<RecordBase.Tag>,
            val isButtonEnabled: Boolean,
        )

        data class SplitParams(
            val newTimeSplit: Long,
            val newBeforeTypeId: Long,
            val splitPreviewTimeEnded: Long,
            val showTimeEndedOnSplitPreview: Boolean,
            val originalTypeId: Long,
            val originalTags: List<RecordBase.Tag>,
        )

        data class DuplicateParams(
            val isAvailable: Boolean,
            val newTimeEnded: Long,
        )

        data class MoveParams(
            val isAvailable: Boolean,
        )

        data class ContinueParams(
            val originalRecordId: Long,
            val isAvailable: Boolean,
        )

        data class RepeatParams(
            val isAvailable: Boolean,
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

        data class ShortcutParams(
            val isAvailable: Boolean,
        )
    }
}