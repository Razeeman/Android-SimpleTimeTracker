package com.example.util.simpletimetracker.feature_base_adapter.empty

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setTextOptional
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemEmptyLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData as ViewData

fun createEmptyAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvEmptyItem.text = item.message
        tvEmptyItemHint.setTextOptional(item.hint)
    }
}