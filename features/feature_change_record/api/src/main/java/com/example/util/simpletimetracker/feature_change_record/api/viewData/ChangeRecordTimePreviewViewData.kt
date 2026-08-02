package com.example.util.simpletimetracker.feature_change_record.api.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordActionsBlock

data class ChangeRecordTimePreviewViewData(
    val block: ChangeRecordActionsBlock,
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordTimePreviewViewData
}