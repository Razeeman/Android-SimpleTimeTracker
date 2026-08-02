package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.api.viewData.ChangeRecordTimeAdjustmentViewData as ViewData
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordTimeAdjustmentItemBinding as Binding

fun createChangeRecordTimeAdjustmentAdapterDelegate(
    onClick: (ViewData, TimeAdjustmentView.ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        root.tag = item.block
        containerChangeRecordTimeAdjustmentItem.adapter.replace(item.items)
        containerChangeRecordTimeAdjustmentItem.listener = { onClick(item, it) }
    }
}