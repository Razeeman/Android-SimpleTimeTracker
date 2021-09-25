package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailPreviewMoreItemBinding as Binding
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewMoreViewData as ViewData

fun createStatisticsPreviewMoreAdapterDelegate() =
    createRecyclerBindingAdapterDelegate<ViewData, Binding>(
        Binding::inflate
    ) { _, _, _ -> /* Nothing to bind */ }
