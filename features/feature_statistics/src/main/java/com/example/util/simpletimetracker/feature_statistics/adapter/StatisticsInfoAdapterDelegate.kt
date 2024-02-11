package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.databinding.ItemStatisticsInfoLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsInfoViewData as ViewData

fun createStatisticsInfoAdapterDelegate() =
    createRecyclerBindingAdapterDelegate<ViewData, Binding>(
        Binding::inflate,
    ) { binding, item, _ ->

        with(binding) {
            item as ViewData

            tvStatisticsInfoName.text = item.name
            tvStatisticsInfoText.text = item.text
        }
    }