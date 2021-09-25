package com.example.util.simpletimetracker.feature_base_adapter.empty

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemEmptyLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData as ViewData

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