package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.feature_statistics_detail.customView.BarChartView

data class StatisticsDetailChartViewData(
    val visible: Boolean,
    val data: List<BarChartView.ViewData>,
    val legendSuffix: String,
    val addLegendToSelectedBar: Boolean,
    val shouldDrawHorizontalLegends: Boolean,
    val goalValue: Float,
)