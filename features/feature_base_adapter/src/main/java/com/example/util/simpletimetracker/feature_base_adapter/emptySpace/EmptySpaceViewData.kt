package com.example.util.simpletimetracker.feature_base_adapter.emptySpace

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class EmptySpaceViewData(
    val id: Long,
    val widthDp: Int = 0,
    val heightDp: Int = 0,
    val wrapBefore: Boolean = false,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is EmptySpaceViewData
}