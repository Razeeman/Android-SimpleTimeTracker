package com.example.util.simpletimetracker.core.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class ColorViewData(
    val colorId: Int,
    @ColorInt val colorInt: Int
) : ViewHolderType {

    override fun getUniqueId(): Long = colorId.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ColorViewData
}