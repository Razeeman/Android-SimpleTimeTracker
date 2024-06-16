package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class StatisticsDetailStatsViewData(
    val totalDuration: List<StatisticsDetailCardInternalViewData>,
    val timesTracked: List<StatisticsDetailCardInternalViewData>,
    val averageRecord: List<StatisticsDetailCardInternalViewData>,
    val datesTracked: List<StatisticsDetailCardInternalViewData>,
    val splitData: List<ViewHolderType>,
)