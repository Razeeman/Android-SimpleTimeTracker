package com.example.util.simpletimetracker.feature_base_adapter.color

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ColorFavouriteViewData(
    @ColorInt val iconColor: Int,
) : ViewHolderType {

    // Only one in recycler
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is ColorFavouriteViewData
}