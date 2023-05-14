package com.example.util.simpletimetracker.feature_records_filter.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterDayOfWeekViewData as ViewData
import com.example.util.simpletimetracker.feature_records_filter.databinding.ItemRecordsFilterDayOfWeekBinding as Binding

fun createRecordsFilterDayOfWeekAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        containerRecordFilterDayOfWeekItem.setCardBackgroundColor(item.color)
        btnRecordsFilterDayOfWeekItem.text = item.text
        btnRecordsFilterDayOfWeekItem.setOnClickWith(item, onClick)
    }
}
