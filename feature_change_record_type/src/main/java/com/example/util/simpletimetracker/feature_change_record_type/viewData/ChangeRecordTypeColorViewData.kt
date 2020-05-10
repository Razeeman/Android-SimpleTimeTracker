package com.example.util.simpletimetracker.feature_change_record_type.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class ChangeRecordTypeColorViewData(
    val colorId: Int,
    @ColorInt val colorInt: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW
}