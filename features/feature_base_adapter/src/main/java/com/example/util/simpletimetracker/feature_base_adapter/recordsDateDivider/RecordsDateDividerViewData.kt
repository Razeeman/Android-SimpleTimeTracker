package com.example.util.simpletimetracker.feature_base_adapter.recordsDateDivider

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordsDateDividerViewData(
    val message: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = message.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is RecordsDateDividerViewData
}