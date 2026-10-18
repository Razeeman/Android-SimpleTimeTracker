package com.example.util.simpletimetracker.feature_base_adapter.color

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ColorFavouriteViewData(
    @DrawableRes val iconResId: Int,
    @ColorInt val backgroundColor: Int,
) : ViewHolderType {

    // Only one in recycler
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is ColorFavouriteViewData
}