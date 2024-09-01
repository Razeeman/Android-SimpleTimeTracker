package com.example.util.simpletimetracker.feature_base_adapter.selectionButton

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

class SelectionButtonViewData(
    val type: Type,
    val name: String,
    @ColorInt val color: Int,
) : ViewHolderType {

    // Only one add item on screen
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean =
        other is SelectionButtonViewData && other.type == type

    interface Type
}