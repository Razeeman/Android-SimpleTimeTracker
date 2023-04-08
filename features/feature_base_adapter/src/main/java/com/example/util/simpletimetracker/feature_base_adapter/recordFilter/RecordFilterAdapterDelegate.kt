package com.example.util.simpletimetracker.feature_base_adapter.recordFilter

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRecordFilterLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData as ViewData

fun createRecordFilterAdapterDelegate(
    onClick: (ViewData) -> Unit,
    onRemoveClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        containerRecordFilter.setCardBackgroundColor(item.color)
        tvActivityFilterItemName.text = item.name
        ivRecordFilterItemRemove.isVisible = item.enabled

        containerRecordFilter.setOnClickWith(item, onClick)
        ivRecordFilterItemRemove.setOnClickWith(item, onRemoveClick)
    }
}