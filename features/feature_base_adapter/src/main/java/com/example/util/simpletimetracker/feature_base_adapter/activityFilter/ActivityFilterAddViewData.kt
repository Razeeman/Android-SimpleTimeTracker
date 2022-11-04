package com.example.util.simpletimetracker.feature_base_adapter.activityFilter

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

class ActivityFilterAddViewData(
    val name: String,
    @ColorInt val color: Int
) : ViewHolderType {

    // Only one add item on screen
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ActivityFilterAddViewData
}