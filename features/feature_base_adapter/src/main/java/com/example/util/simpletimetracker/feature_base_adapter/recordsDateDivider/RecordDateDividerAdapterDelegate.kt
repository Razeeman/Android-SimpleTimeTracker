package com.example.util.simpletimetracker.feature_base_adapter.recordsDateDivider

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRecordsDateDividerLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.recordsDateDivider.RecordsDateDividerViewData as ViewData

fun createRecordsDateDividerAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemRecordsDateDivider.text = item.message
    }
}