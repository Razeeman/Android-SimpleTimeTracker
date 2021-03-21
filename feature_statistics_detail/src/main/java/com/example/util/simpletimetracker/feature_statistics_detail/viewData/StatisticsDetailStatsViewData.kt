package com.example.util.simpletimetracker.feature_statistics_detail.viewData

data class StatisticsDetailStatsViewData(
    val totalDuration: List<StatisticsDetailCardViewData>,
    val timesTracked: List<StatisticsDetailCardViewData>,
    val averageRecord: List<StatisticsDetailCardViewData>,
    val datesTracked: List<StatisticsDetailCardViewData>
)