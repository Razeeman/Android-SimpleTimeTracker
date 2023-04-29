package com.example.util.simpletimetracker.feature_widget.single.settings.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData as ViewData
import com.example.util.simpletimetracker.feature_widget.databinding.ItemWidgetRecordTypeLayoutBinding as Binding

fun createWidgetSingleSettingsAdapterDelegate(
    onItemClick: ((ViewData) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewRecordTypeItem) {
        item as ViewData

        itemColor = item.color
        itemIcon = item.iconId
        itemIconColor = item.iconColor
        itemIconAlpha = item.iconAlpha
        itemName = item.name
        setOnClickWith(item, onItemClick)
    }
}