package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSimpleViewData

data class ChangeRecordChangePreviewViewData(
    val id: Long,
    val before: ChangeRecordSimpleViewData,
    val after: ChangeRecordSimpleViewData,
    val isChecked: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordChangePreviewViewData
}