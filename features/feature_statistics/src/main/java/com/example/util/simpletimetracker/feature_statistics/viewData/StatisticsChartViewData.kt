package com.example.util.simpletimetracker.feature_statistics.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.pieChart.PiePortion

data class StatisticsChartViewData(
    val data: List<PiePortion>
) : ViewHolderType {

    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is StatisticsChartViewData
}