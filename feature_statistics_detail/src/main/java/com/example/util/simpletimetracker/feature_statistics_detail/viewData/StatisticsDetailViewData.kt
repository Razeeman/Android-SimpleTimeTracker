package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class StatisticsDetailViewData(
    val name: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int,
    val totalDuration: String,
    val timesTracked: String
)