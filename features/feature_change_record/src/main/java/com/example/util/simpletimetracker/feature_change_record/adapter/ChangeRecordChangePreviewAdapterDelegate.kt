package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.utils.setData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData as ViewData
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordPreviewLayoutBinding as Binding

fun createChangeRecordChangePreviewAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        viewChangeRecordPreviewBefore.setData(item.before)
        viewChangeRecordPreviewAfter.setData(item.after)
    }
}