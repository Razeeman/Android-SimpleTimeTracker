package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength

data class StatisticsDetailChartCompositeViewData(
    val data: List<StatisticsDetailViewData<*>>,
    val appliedChartGrouping: ChartGrouping,
    val appliedChartLength: ChartLength,
)