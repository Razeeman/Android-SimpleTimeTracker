package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_statistics.databinding.ItemStatisticsChartLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData as ViewData
import androidx.core.view.isVisible

fun createStatisticsChartAdapterDelegate(
    onFilterClick: (() -> Unit),
    onShareClick: (() -> Unit),
    onChartAttached: (Boolean) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        chartStatisticsItem.setSegments(
            data = item.data,
            animateOpen = item.animatedOpen,
        )
        chartStatisticsItem.setAttachedListener(onChartAttached)

        btnStatisticsChartFilter.isVisible = item.buttonsVisible
        btnStatisticsChartShare.isVisible = item.buttonsVisible

        btnStatisticsChartFilter.setOnClick(onFilterClick)
        btnStatisticsChartShare.setOnClick(onShareClick)
    }
}