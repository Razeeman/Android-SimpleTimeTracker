package com.example.util.simpletimetracker.feature_statistics_detail.viewData

object StatisticsDetailClickableTracked : StatisticsDetailCardInternalViewData.ClickableType

data class StatisticsDetailClickableShortest(
    val message: String,
) : StatisticsDetailCardInternalViewData.ClickableType

data class StatisticsDetailClickableLongest(
    val message: String,
) : StatisticsDetailCardInternalViewData.ClickableType