package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreview
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClickWith
import com.example.util.simpletimetracker.feature_views.extension.setTextOptional
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailPreviewItemBinding as Binding
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData as ViewData

fun createStatisticsPreviewAdapterDelegate(
    onClick: ((StatisticsDetailPreview) -> Unit)? = null,
    onLongClick: ((StatisticsDetailPreview) -> Unit)? = null,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        layoutStatisticsDetailPreviewItem.setCardBackgroundColor(item.color)
        tvStatisticsDetailPreviewItemName.setTextOptional(item.name)
        ivStatisticsDetailPreviewItemIcon.visible = item.iconId != null
        item.iconId?.let(ivStatisticsDetailPreviewItemIcon::itemIcon::set)
        item.iconColor?.let(ivStatisticsDetailPreviewItemIcon::itemIconColor::set)
        ivStatisticsDetailPreviewItemIcon.itemIconAlpha = item.iconAlpha

        onClick?.let { root.setOnClickWith(item, it) }
        onLongClick?.let { root.setOnLongClickWith(item, it) }
    }
}