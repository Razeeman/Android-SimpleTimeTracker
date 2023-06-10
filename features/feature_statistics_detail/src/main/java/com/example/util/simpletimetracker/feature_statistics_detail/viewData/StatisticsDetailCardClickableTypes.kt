package com.example.util.simpletimetracker.feature_statistics_detail.viewData

object StatisticsDetailClickableTracked : StatisticsDetailCardViewData.ClickableType

data class StatisticsDetailClickableShortest(
    val message: String,
) : StatisticsDetailCardViewData.ClickableType

data class StatisticsDetailClickableLongest(
    val message: String,
) : StatisticsDetailCardViewData.ClickableType