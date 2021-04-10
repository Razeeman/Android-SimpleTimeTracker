package com.example.util.simpletimetracker.core.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class CategoryViewData(
    val id: Long,
    val name: String,
    @ColorInt val textColor: Int,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is CategoryViewData
}