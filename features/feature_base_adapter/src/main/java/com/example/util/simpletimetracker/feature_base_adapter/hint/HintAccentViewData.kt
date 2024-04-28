package com.example.util.simpletimetracker.feature_base_adapter.hint

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class HintAccentViewData(
    val text: String,
    val paddingTop: Int = 8,
    val paddingBottom: Int = 8,
) : ViewHolderType {

    override fun getUniqueId(): Long = text.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is HintAccentViewData
}