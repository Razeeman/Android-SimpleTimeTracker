package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class StatisticsDetailPreviewViewData(
    val name: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int
)