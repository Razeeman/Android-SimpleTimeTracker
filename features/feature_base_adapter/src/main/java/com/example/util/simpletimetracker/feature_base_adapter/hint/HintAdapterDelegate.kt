package com.example.util.simpletimetracker.feature_base_adapter.hint

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemHintLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData as ViewData

fun createHintAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvHintItemText.text = item.text
    }
}