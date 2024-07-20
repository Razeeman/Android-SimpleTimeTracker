package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeAdjustmentViewData as ViewData
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

data class ChangeRecordTimeAdjustmentViewData(
    val block: ChangeRecordActionsBlock,
    val items: List<ViewHolderType>,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData
}