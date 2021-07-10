package com.example.util.simpletimetracker.core.adapter.info

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.info.InfoViewData as ViewData
import com.example.util.simpletimetracker.core.databinding.ItemInfoLayoutBinding as Binding

fun createInfoAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvInfoItemText.text = item.text
    }
}