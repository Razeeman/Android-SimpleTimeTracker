package com.example.util.simpletimetracker.feature_base_adapter.emptySpace

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class EmptySpaceViewData(
    val id: Long,
    val width: ViewDimension = ViewDimension.ExactSizeDp(0),
    val height: ViewDimension = ViewDimension.ExactSizeDp(0),
    val wrapBefore: Boolean = false,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is EmptySpaceViewData

    sealed interface ViewDimension {
        object MatchParent : ViewDimension
        object WrapContent : ViewDimension
        data class ExactSizeDp(val value: Int) : ViewDimension
    }
}