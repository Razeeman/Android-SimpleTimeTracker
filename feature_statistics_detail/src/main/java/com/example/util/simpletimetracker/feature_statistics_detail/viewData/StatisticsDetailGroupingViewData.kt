package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.ChartGrouping

data class StatisticsDetailGroupingViewData(
    val chartGrouping: ChartGrouping,
    override val name: String,
    override val isSelected: Boolean
) : ButtonsRowViewData() {

    override val id: Long = chartGrouping.ordinal.toLong()
}