package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ChangeRecordTypeIconCategoryInfoViewData(
    val type: ChangeRecordTypeIconTypeViewData,
    val text: String
) : ViewHolderType {

    override fun getUniqueId(): Long = type.id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordTypeIconCategoryInfoViewData
}