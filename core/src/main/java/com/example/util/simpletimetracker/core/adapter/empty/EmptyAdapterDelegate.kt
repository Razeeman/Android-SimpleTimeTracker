package com.example.util.simpletimetracker.core.adapter.empty

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData as ViewData
import com.example.util.simpletimetracker.core.databinding.ItemEmptyLayoutBinding as Binding

fun createEmptyAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvEmptyItem.text = item.message

        if (item.hint.isNotEmpty()) {
            tvEmptyItemHint.visible = true
            tvEmptyItemHint.text = item.hint
        } else {
            tvEmptyItemHint.visible = false
        }
    }
}