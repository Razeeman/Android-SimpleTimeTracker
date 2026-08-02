package com.example.util.simpletimetracker.feature_change_record.api.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.api.model.TimeAdjustmentState

data class ChangeRecordTimeDoublePreviewViewData(
    val block: ChangeRecordActionsBlock,
    val dateTimeStarted: String,
    val dateTimeFinished: String,
    val isTimeEndedAvailable: Boolean,
    val state: TimeAdjustmentState,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordTimeDoublePreviewViewData
}