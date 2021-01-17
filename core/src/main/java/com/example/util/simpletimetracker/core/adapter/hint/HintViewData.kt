package com.example.util.simpletimetracker.core.adapter.hint

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class HintViewData(
    val text: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.INFO

    override fun getUniqueId(): Long? = text.hashCode().toLong()
}