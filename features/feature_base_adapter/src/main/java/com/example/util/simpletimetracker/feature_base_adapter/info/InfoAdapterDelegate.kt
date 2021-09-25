package com.example.util.simpletimetracker.feature_base_adapter.info

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemInfoLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData as ViewData

fun createInfoAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvInfoItemText.text = item.text
    }
}