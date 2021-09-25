package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class StatisticsDetailCardViewData(
    val title: String,
    val subtitle: String,
    val icon: Icon? = null
) : ViewHolderType {

    override fun getUniqueId(): Long = title.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is StatisticsDetailCardViewData

    data class Icon(
        @DrawableRes val iconDrawable: Int,
        @ColorInt val iconColor: Int
    )
}