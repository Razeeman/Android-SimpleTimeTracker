package com.example.util.simpletimetracker.feature_records_all.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_records_all.databinding.ItemRecordsAllDateLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllDateViewData as ViewData

fun createRecordAllDateAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvRecordsAllDate.text = item.message
    }
}