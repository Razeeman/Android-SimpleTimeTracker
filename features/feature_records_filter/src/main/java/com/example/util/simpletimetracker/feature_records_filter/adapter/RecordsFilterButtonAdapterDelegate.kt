package com.example.util.simpletimetracker.feature_records_filter.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_records_filter.databinding.ItemRecordsFilterButtonBinding as Binding

fun createRecordsFilterButtonAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        btnRecordsFilterButtonItem.text = item.text
        btnRecordsFilterButtonItem.setOnClickWith(item, onClick)
    }
}
