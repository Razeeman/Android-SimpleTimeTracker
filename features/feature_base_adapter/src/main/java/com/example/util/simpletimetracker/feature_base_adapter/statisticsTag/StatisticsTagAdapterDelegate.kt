package com.example.util.simpletimetracker.feature_base_adapter.statisticsTag

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemStatisticsTagLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.statisticsTag.StatisticsTagViewData as ViewData

fun createStatisticsTagAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewStatisticsTagItem) {
        item as ViewData

        itemColor = item.color
        itemName = item.name
        itemDuration = item.duration
        itemPercent = item.percent
        itemIconVisible = true
        itemIcon = item.icon
    }
}
