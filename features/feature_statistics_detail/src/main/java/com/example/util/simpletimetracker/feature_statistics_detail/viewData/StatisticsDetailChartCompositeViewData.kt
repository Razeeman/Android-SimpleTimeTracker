package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength

data class StatisticsDetailChartCompositeViewData(
    val chartData: StatisticsDetailChartViewData,
    val rangeAveragesTitle: String,
    val rangeAverages: List<StatisticsDetailCardViewData>,
    val appliedChartGrouping: ChartGrouping,
    val chartGroupingViewData: List<ViewHolderType>,
    val appliedChartLength: ChartLength,
    val chartLengthViewData: List<ViewHolderType>,
)