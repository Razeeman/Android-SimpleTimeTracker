package com.example.util.simpletimetracker.core.delegates.iconSelection.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ChangeRecordTypeIconCategoryInfoViewData(
    val type: ChangeRecordTypeIconTypeViewData,
    val text: String,
    val isLast: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = type.id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordTypeIconCategoryInfoViewData
}