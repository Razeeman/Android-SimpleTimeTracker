package com.example.util.simpletimetracker.feature_change_record.api.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordActionsBlock

data class ChangeRecordTimeAdjustmentViewData(
    val block: ChangeRecordActionsBlock,
    val items: List<ViewHolderType>,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordTimeAdjustmentViewData
}