package com.example.util.simpletimetracker.feature_base_adapter.divider

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class DividerViewData(
    val id: Long,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is DividerViewData
}