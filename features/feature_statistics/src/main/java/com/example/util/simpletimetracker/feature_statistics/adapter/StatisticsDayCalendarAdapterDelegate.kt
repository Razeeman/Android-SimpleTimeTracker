package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.databinding.ItemStatisticsDayCalendarLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsDayCalendarViewData as ViewData

fun createStatisticsDayCalendarAdapterDelegate() =
    createRecyclerBindingAdapterDelegate<ViewData, Binding>(
        Binding::inflate,
    ) { binding, item, _ ->

        with(binding) {
            item as ViewData

            viewStatisticsDayCalendarItem.setData(item.data)
        }
    }