package com.example.util.simpletimetracker.core.adapter.empty

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class EmptyViewData(
    val message: String,
    val hint: String = ""
) : ViewHolderType {

    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is EmptyViewData
}