package com.example.util.simpletimetracker.feature_statistics_detail.viewData

data class StatisticsDetailChartCompositeViewData(
    val chartData: StatisticsDetailChartViewData,
    val rangeAveragesTitle: String,
    val rangeAverages: List<StatisticsDetailCardViewData>,
)