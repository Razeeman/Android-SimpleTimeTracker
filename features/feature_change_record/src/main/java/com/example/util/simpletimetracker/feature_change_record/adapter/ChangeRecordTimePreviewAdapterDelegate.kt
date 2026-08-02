package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_change_record.api.viewData.ChangeRecordTimePreviewViewData as ViewData
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordTimePreviewItemBinding as Binding

fun createChangeRecordTimePreviewAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvChangeRecordTimePreviewItem.text = item.text
        fieldChangeRecordTimePreviewItem.setOnClick { onClick(item) }
    }
}