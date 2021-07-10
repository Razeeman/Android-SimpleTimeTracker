package com.example.util.simpletimetracker.core.adapter.color

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.databinding.ItemColorLayoutBinding as Binding
import com.example.util.simpletimetracker.core.viewData.ColorViewData as ViewData

fun createColorAdapterDelegate(
    onColorItemClick: ((ViewData) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        layoutColorItem.setCardBackgroundColor(item.colorInt)
        layoutColorItem.setOnClickWith(item, onColorItemClick)
    }
}