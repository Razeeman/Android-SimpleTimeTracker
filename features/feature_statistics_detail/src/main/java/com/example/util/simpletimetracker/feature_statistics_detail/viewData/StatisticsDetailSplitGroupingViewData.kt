package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping

data class StatisticsDetailSplitGroupingViewData(
    val splitChartGrouping: SplitChartGrouping,
    override val name: String,
    override val isSelected: Boolean,
) : ButtonsRowViewData() {

    override val id: Long = splitChartGrouping.ordinal.toLong()
}