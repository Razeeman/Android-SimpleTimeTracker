package com.example.util.simpletimetracker.feature_base_adapter.activityFilter

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ActivityFilterViewData(
    val id: Long,
    val name: String,
    @ColorInt val iconColor: Int,
    @ColorInt val color: Int,
    val selected: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ActivityFilterViewData
}