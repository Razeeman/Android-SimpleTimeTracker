package com.example.util.simpletimetracker.core.adapter.divider

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

object DividerViewData : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.DIVIDER

    // Only one item on screen
    override fun getUniqueId(): Long? = 1L
}