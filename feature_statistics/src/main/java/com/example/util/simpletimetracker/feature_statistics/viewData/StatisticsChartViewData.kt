package com.example.util.simpletimetracker.feature_statistics.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics.customView.PiePortion

data class StatisticsChartViewData(
    val data: List<PiePortion>
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.HEADER

    override fun getUniqueId(): Long? = 1L
}