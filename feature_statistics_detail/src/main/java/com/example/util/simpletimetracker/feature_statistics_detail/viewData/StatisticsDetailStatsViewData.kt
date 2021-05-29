package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsDetailStatsViewData(
    val totalDuration: List<StatisticsDetailCardViewData>,
    val timesTracked: List<StatisticsDetailCardViewData>,
    val averageRecord: List<StatisticsDetailCardViewData>,
    val datesTracked: List<StatisticsDetailCardViewData>,
    val tagSplitData: List<ViewHolderType>
)