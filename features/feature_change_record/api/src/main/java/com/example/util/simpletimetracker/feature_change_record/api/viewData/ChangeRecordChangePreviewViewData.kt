package com.example.util.simpletimetracker.feature_change_record.api.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ChangeRecordChangePreviewViewData(
    val id: Long,
    val before: ChangeRecordSimpleViewData,
    val after: ChangeRecordSimpleViewData,
    val isChecked: Boolean,
    val marginTopDp: Int,
    val isRemoveVisible: Boolean,
    val isCheckVisible: Boolean,
    val isCompareVisible: Boolean,
    val isBeforeActionVisible: Boolean,
    val isAfterActionVisible: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordChangePreviewViewData
}