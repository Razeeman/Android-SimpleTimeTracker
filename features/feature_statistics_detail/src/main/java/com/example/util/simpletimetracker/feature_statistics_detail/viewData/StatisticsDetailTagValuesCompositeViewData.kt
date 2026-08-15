package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength

data class StatisticsDetailTagValuesCompositeViewData(
    val viewData: List<StatisticsDetailViewData.Item<*>>,
    val appliedChartGrouping: ChartGrouping,
    val appliedChartLength: ChartLength,
)