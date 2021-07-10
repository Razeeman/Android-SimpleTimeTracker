package com.example.util.simpletimetracker.core.adapter.hint

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.hint.HintViewData as ViewData
import com.example.util.simpletimetracker.core.databinding.ItemHintLayoutBinding as Binding

fun createHintAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvHintItemText.text = item.text
    }
}