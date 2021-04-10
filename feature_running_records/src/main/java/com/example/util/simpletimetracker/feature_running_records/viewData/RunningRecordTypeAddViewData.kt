package com.example.util.simpletimetracker.feature_running_records.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class RunningRecordTypeAddViewData(
    val name: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int,
    val width: Int,
    val height: Int,
    val asRow: Boolean = false
) : ViewHolderType {

    // Only one add item on screen
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is RunningRecordTypeAddViewData
}