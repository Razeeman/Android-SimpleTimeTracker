package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailPreviewCompareItemBinding as Binding
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompareViewData as ViewData

fun createStatisticsPreviewCompareAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { _, _, _ ->
    // Nothing to bind
}
