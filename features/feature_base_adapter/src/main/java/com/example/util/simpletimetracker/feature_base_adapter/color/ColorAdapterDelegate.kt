package com.example.util.simpletimetracker.feature_base_adapter.color

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemColorLayoutBinding as Binding

fun createColorAdapterDelegate(
    onColorItemClick: ((ViewData) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        layoutColorItem.setCardBackgroundColor(item.colorInt)
        viewColorItemSelected.visible = item.selected
        layoutColorItem.setOnClickWith(item, onColorItemClick)
    }
}