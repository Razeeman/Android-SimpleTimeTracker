package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData as ViewData
import com.example.util.simpletimetracker.feature_statistics.databinding.ItemStatisticsEmptyLayoutBinding as Binding

fun createStatisticsEmptyAdapterDelegate(
    onFilterClick: (() -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvEmptyItem.text = item.message
        btnStatisticsEmptyFilter.setOnClick(onFilterClick)
    }
}