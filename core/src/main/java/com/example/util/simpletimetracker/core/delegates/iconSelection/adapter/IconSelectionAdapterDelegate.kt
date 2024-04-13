package com.example.util.simpletimetracker.core.delegates.iconSelection.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.core.databinding.ItemIconSelectionLayoutBinding as Binding
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionViewData as ViewData

fun createIconSelectionAdapterDelegate(
    onIconItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        layoutIconSelectionItem.setCardBackgroundColor(item.colorInt)
        ivIconSelectionItem.setBackgroundResource(item.iconResId)
        ivIconSelectionItem.tag = item.iconResId
        layoutIconSelectionItem.setOnClickWith(item, onIconItemClick)
    }
}