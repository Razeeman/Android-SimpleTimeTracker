package com.example.util.simpletimetracker.feature_categories.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

object CategoryDividerViewData : ViewHolderType {

    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean =
        other is CategoryDividerViewData
}