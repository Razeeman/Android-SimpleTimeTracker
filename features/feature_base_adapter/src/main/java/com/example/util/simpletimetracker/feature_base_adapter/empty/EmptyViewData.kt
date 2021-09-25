package com.example.util.simpletimetracker.feature_base_adapter.empty

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class EmptyViewData(
    val message: String,
    val hint: String = ""
) : ViewHolderType {

    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is EmptyViewData
}