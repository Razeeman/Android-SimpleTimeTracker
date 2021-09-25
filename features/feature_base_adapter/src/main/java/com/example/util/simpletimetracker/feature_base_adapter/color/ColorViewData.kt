package com.example.util.simpletimetracker.feature_base_adapter.color

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ColorViewData(
    val colorId: Int,
    @ColorInt val colorInt: Int
) : ViewHolderType {

    override fun getUniqueId(): Long = colorId.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ColorViewData
}