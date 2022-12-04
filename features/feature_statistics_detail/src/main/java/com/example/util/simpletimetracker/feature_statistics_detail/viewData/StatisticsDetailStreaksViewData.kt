package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesView

data class StatisticsDetailStreaksViewData(
    val streaks: List<StatisticsDetailCardViewData>,
    val data: List<SeriesView.ViewData>,
)