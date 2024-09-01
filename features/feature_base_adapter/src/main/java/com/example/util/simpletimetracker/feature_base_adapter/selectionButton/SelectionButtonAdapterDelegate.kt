package com.example.util.simpletimetracker.feature_base_adapter.selectionButton

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemSelectionButtonLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.selectionButton.SelectionButtonViewData as ViewData

fun createSelectionButtonAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        root.setCardBackgroundColor(item.color)
        tvSelectionButtonItemName.text = item.name
        root.setOnClickWith(item, onItemClick)
    }
}