package com.example.util.simpletimetracker.feature_change_record_type.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class ChangeRecordTypeIconViewData(
    val iconId: Int,
    @DrawableRes val iconResId: Int,
    @ColorInt val colorInt: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW2
}