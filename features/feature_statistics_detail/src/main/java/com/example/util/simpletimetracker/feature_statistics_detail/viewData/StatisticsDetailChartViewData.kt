package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.domain.base.OneShotValue
import com.example.util.simpletimetracker.feature_views.barChart.BarChartView

data class StatisticsDetailChartViewData(
    val data: List<BarChartView.ViewData>,
    val legendSuffix: String,
    val addLegendToSelectedBar: Boolean,
    val shouldDrawHorizontalLegends: Boolean,
    val showSelectedBarOnStart: Boolean,
    val selectedBarPosition: Int?,
    val goalValue: Float,
    val yAxisZoomed: Boolean,
    val useSingleColor: Boolean,
    val drawRoundCaps: Boolean,
    val animate: OneShotValue<Boolean>,
)