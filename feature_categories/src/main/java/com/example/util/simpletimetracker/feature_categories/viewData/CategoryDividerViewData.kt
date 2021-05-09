package com.example.util.simpletimetracker.feature_categories.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class CategoryDividerViewData(
    val id: Long
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is CategoryDividerViewData
}