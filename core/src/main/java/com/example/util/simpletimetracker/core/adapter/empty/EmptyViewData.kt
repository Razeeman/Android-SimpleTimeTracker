package com.example.util.simpletimetracker.core.adapter.empty

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class EmptyViewData(
    var message: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.EMPTY

    override fun getUniqueId(): Long? = 1L
}