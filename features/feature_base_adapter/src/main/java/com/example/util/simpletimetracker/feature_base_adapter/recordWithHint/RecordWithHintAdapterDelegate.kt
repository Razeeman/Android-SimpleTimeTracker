package com.example.util.simpletimetracker.feature_base_adapter.recordWithHint

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.record.bindState
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRecordWithHintLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.recordWithHint.RecordWithHintViewData as ViewData

fun createRecordWithHintAdapterDelegate(
    onItemClick: (ViewData) -> Unit,
    onItemLongClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.viewRecordItem) {
        item as ViewData

        bindState(item.record)
        setOnClick { onItemClick(item) }
        setOnLongClick { onItemLongClick(item) }
    }
}