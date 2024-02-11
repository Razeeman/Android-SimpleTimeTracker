package com.example.util.simpletimetracker.feature_base_adapter.multitaskRecord

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemMultitaskRecordLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.multitaskRecord.MultitaskRecordViewData as ViewData

fun createMultitaskRecordAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.root) {
        item as ViewData
        setData(item.data)
    }
}