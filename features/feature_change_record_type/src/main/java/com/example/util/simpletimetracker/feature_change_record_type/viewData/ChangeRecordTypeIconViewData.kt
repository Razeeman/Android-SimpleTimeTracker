package com.example.util.simpletimetracker.feature_change_record_type.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class ChangeRecordTypeIconViewData(
    val iconName: String,
    @DrawableRes val iconResId: Int,
    @ColorInt val colorInt: Int
) : ViewHolderType {

    override fun getUniqueId(): Long = iconName.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ChangeRecordTypeIconViewData
}