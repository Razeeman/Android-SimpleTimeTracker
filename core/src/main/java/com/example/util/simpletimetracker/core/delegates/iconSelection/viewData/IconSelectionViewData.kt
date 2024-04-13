package com.example.util.simpletimetracker.core.delegates.iconSelection.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class IconSelectionViewData(
    val iconName: String,
    @DrawableRes val iconResId: Int,
    @ColorInt val colorInt: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = iconName.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is IconSelectionViewData
}