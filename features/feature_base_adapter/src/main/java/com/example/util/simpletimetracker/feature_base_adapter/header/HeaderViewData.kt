package com.example.util.simpletimetracker.feature_base_adapter.header

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class HeaderViewData(
    val section: Section,
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = section.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is HeaderViewData

    interface Section
}