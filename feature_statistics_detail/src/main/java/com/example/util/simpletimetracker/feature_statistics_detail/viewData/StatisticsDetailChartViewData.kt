package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.ColorInt

data class StatisticsDetailChartViewData(
    val data: List<Long>,
    @ColorInt val dailyButtonColor: Int,
    @ColorInt val weeklyButtonColor: Int,
    @ColorInt val monthlyButtonColor: Int
)