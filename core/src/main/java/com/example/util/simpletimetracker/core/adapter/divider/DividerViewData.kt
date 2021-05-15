package com.example.util.simpletimetracker.core.adapter.divider

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class DividerViewData(
    val id: Long
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is DividerViewData
}