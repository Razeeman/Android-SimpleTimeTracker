package com.example.util.simpletimetracker.feature_base_adapter.recordFilter

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setTextOptional
import com.example.util.simpletimetracker.feature_views.databinding.ItemFilterLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData as ViewData

fun createFilterAdapterDelegate(
    onClick: (ViewData) -> Unit,
    onButtonClick: (ViewData) -> Unit = {},
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        containerFilter.setCardBackgroundColor(item.color)
        tvFilterItemName.setTextOptional(item.name)
        ivFilterItemButton.isVisible = item.isBtnVisible
        ivFilterItemButton.setImageResource(item.customBtnIconResId ?: R.drawable.ic_remove)
        cardFilterBackground.isVisible = item.selected

        containerFilter.setOnClickWith(item, onClick)
        ivFilterItemButton.setOnClickWith(item, onButtonClick)
    }
}