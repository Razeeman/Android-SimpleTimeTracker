package com.example.util.simpletimetracker.feature_base_adapter.hint

import androidx.core.view.updatePadding
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemHintAccentLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintAccentViewData as ViewData

fun createHintAccentAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvHintItemText.updatePadding(
            top = item.paddingTop.dpToPx(),
            bottom = item.paddingBottom.dpToPx(),
        )
        tvHintItemText.text = item.text
    }
}