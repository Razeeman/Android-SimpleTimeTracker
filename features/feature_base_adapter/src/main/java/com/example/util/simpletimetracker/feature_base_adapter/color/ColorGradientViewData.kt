package com.example.util.simpletimetracker.feature_base_adapter.color

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ColorGradientViewData(
    val selected: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is ColorGradientViewData
}
