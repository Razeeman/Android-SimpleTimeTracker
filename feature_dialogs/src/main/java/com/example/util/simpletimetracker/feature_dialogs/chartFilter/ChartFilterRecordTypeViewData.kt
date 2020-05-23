package com.example.util.simpletimetracker.feature_dialogs.chartFilter

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class ChartFilterRecordTypeViewData(
    val id: Long,
    val name: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW
}