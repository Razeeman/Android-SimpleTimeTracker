package com.example.util.simpletimetracker.feature_base_adapter.color

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorPaletteViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemColorPaletteLayoutBinding as Binding

fun createColorPaletteAdapterDelegate(
    onColorPaletteItemClick: (() -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        viewColorPaletteItemSelected.visible = item.selected
        layoutColorPaletteItem.setOnClick(onColorPaletteItemClick)
    }
}