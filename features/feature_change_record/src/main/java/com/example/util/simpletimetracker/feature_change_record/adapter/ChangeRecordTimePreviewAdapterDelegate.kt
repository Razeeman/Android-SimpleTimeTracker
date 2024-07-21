package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimePreviewViewData as ViewData
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

data class ChangeRecordTimePreviewViewData(
    val block: ChangeRecordActionsBlock,
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData
}