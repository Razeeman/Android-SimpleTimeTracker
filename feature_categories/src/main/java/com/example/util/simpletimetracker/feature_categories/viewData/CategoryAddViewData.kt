package com.example.util.simpletimetracker.feature_categories.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.model.TagType

class CategoryAddViewData(
    val type: TagType,
    val name: String,
    @ColorInt val color: Int
) : ViewHolderType {

    // Only one add item on screen
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean =
        other is CategoryAddViewData && other.type == type
}