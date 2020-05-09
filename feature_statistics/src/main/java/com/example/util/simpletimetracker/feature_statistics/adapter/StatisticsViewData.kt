package com.example.util.simpletimetracker.feature_statistics.adapter

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsViewData(
    var name: String,
    var duration: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW
}