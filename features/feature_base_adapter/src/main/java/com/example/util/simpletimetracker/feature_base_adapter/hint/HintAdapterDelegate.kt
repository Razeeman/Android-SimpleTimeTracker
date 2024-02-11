package com.example.util.simpletimetracker.feature_base_adapter.hint

import androidx.core.view.updatePadding
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemHintLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData as ViewData

fun createHintAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        val padding = item.paddingVertical.dpToPx()
        tvHintItemText.updatePadding(top = padding, bottom = padding)
        tvHintItemText.text = item.text
    }
}