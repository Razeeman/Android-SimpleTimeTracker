package com.example.util.simpletimetracker.feature_categories.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class CategoryAddViewData(
    val name: String,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.FOOTER

    // Only one add item on screen
    override fun getUniqueId(): Long? = 1L
}