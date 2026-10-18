package com.example.util.simpletimetracker.feature_base_adapter.color

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorGradientViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemColorGradientLayoutBinding as Binding

fun createColorGradientAdapterDelegate(
    onClick: () -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->
    with(binding) {
        item as ViewData
        layoutColorGradientItem.setCardBackgroundColor(item.backgroundColor)
        layoutColorGradientItem.isSelected = item.selected
        layoutColorGradientItem.setOnClick(onClick)
    }
}
