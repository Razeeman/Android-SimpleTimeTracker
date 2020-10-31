package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsDetailCardViewData(
    val title: String,
    val subtitle: String,
    @DrawableRes val icon: Int? = null
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW
}