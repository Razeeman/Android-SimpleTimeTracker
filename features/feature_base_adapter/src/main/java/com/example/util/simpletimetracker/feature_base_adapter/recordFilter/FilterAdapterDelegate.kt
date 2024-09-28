package com.example.util.simpletimetracker.feature_base_adapter.recordFilter

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemFilterLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData as ViewData

fun createFilterAdapterDelegate(
    onClick: (ViewData) -> Unit,
    onRemoveClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        containerFilter.setCardBackgroundColor(item.color)
        tvFilterItemName.text = item.name
        ivFilterItemRemove.isVisible = item.removeBtnVisible
        cardFilterBackground.isVisible = item.selected

        containerFilter.setOnClickWith(item, onClick)
        ivFilterItemRemove.setOnClickWith(item, onRemoveClick)
    }
}