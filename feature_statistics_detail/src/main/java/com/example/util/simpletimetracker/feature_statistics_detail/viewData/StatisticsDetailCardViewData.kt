package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsDetailCardViewData(
    val title: String,
    val subtitle: String,
    val icon: Icon? = null
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW

    data class Icon(
        @DrawableRes val iconDrawable: Int,
        @ColorInt val iconColor: Int
    )
}