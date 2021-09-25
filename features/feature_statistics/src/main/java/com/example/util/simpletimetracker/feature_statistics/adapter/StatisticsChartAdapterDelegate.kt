package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_statistics.databinding.ItemStatisticsChartLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData as ViewData

fun createStatisticsChartAdapterDelegate(
    onFilterClick: (() -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        chartStatisticsItem.setSegments(item.data)
        btnStatisticsChartFilter.setOnClick(onFilterClick)
    }
}