package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailPreviewItemBinding as Binding
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData as ViewData

fun createStatisticsPreviewAdapterDelegate() =
    createRecyclerBindingAdapterDelegate<ViewData, Binding>(
        Binding::inflate
    ) { binding, item, _ ->

        with(binding) {
            item as ViewData

            layoutStatisticsDetailPreviewItem.setCardBackgroundColor(item.color)
            tvStatisticsDetailPreviewItemName.visible = item.name.isNotEmpty()
            item.name.takeIf { it.isNotEmpty() }
                ?.let(tvStatisticsDetailPreviewItemName::setText)
            ivStatisticsDetailPreviewItemIcon.visible = item.iconId != null
            item.iconId?.let(ivStatisticsDetailPreviewItemIcon::itemIcon::set)
        }
    }