package com.example.util.simpletimetracker.feature_base_adapter.hintBig

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemHintBigLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData as ViewData

fun createHintBigAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvHintItemText.text = item.text
    }
}