package com.example.util.simpletimetracker.core.delegates.iconSelection.viewData

import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class IconSelectionCategoryViewData(
    val type: IconSelectionTypeViewData,
    @DrawableRes val categoryIcon: Int,
    val selected: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = type.id

    override fun isValidType(other: ViewHolderType): Boolean = other is IconSelectionCategoryViewData
}