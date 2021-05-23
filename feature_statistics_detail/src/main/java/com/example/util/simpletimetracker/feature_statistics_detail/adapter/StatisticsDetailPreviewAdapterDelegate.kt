package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import kotlinx.android.synthetic.main.statistics_detail_preview_item.view.*

fun createStatisticsPreviewAdapterDelegate() =
    createRecyclerAdapterDelegate<StatisticsDetailPreviewViewData>(
        R.layout.statistics_detail_preview_item
    ) { itemView, item, _ ->

        with(itemView) {
            item as StatisticsDetailPreviewViewData

            layoutStatisticsDetailPreviewItem.setCardBackgroundColor(item.color)
            tvStatisticsDetailPreviewItemName.visible = item.name.isNotEmpty()
            item.name.takeIf { it.isNotEmpty() }
                ?.let(tvStatisticsDetailPreviewItemName::setText)
            ivStatisticsDetailPreviewItemIcon.visible = item.iconId != null
            item.iconId?.let(ivStatisticsDetailPreviewItemIcon::itemIcon::set)
        }
    }