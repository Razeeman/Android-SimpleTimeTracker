package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class ChangeRecordTypeIconSwitchViewData(
    val data: List<ViewHolderType>
) : ViewHolderType {

    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordTypeIconSwitchViewData
}