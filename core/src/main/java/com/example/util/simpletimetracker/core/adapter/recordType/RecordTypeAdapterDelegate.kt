package com.example.util.simpletimetracker.core.adapter.recordType

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.core.databinding.ItemRecordTypeLayoutBinding as Binding
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData as ViewData

fun createRecordTypeAdapterDelegate(
    onItemClick: ((ViewData) -> Unit)? = null
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewRecordTypeItem) {
        item as ViewData

        layoutParams = layoutParams.also { params ->
            item.width?.dpToPx()?.let { params.width = it }
            item.height?.dpToPx()?.let { params.height = it }
        }

        itemIsRow = item.asRow
        itemColor = item.color
        itemIcon = item.iconId
        itemIconColor = item.iconColor
        itemIconAlpha = item.iconAlpha
        itemName = item.name
        onItemClick?.let { setOnClickWith(item, onItemClick) }
    }
}