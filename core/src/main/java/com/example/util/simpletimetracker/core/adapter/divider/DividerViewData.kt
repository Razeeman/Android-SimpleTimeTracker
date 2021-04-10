package com.example.util.simpletimetracker.core.adapter.divider

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

object DividerViewData : ViewHolderType {

    // Only one item on screen
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is DividerViewData
}