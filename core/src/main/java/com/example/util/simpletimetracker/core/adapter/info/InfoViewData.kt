package com.example.util.simpletimetracker.core.adapter.info

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class InfoViewData(
    val text: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.INFO

    // Only one in recycler, add id if needed, but don't do text hashcode,
    // otherwise tag recycler items on change type will disappear after selecting all and
    // removing all (same in type selection in change category).
    override fun getUniqueId(): Long? = 1L
}