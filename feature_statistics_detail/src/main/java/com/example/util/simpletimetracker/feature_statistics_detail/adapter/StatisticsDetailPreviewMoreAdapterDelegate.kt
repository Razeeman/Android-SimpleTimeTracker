package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewMoreViewData

fun createStatisticsPreviewMoreAdapterDelegate() =
    createRecyclerAdapterDelegate<StatisticsDetailPreviewMoreViewData>(
        R.layout.statistics_detail_preview_more_item
    ) { _, _, _ -> /* Nothing to bind */ }
