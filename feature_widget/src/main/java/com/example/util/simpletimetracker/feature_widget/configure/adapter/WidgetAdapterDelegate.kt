package com.example.util.simpletimetracker.feature_widget.configure.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData as ViewData
import com.example.util.simpletimetracker.feature_widget.databinding.ItemWidgetRecordTypeLayoutBinding as Binding

fun createWidgetAdapterDelegate(
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