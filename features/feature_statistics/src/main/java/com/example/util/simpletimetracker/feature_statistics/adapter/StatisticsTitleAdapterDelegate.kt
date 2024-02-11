package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.feature_statistics.databinding.ItemStatisticsTitleLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsTitleViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate

fun createStatisticsTitleAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvStatisticsTitleItem.text = item.text
    }
}