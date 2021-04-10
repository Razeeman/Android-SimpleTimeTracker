package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData
import kotlinx.android.synthetic.main.item_statistics_chart_layout.view.btnStatisticsChartFilter
import kotlinx.android.synthetic.main.item_statistics_chart_layout.view.chartStatisticsItem

fun createStatisticsChartAdapterDelegate(
    onFilterClick: (() -> Unit)
) = createRecyclerAdapterDelegate<StatisticsChartViewData>(
    R.layout.item_statistics_chart_layout
) { itemView, item, _ ->

    with(itemView) {
        item as StatisticsChartViewData

        chartStatisticsItem.setSegments(item.data)
        btnStatisticsChartFilter.setOnClick(onFilterClick)
    }
}