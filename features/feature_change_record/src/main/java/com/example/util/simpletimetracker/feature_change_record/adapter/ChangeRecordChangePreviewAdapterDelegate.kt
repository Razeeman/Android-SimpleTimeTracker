package com.example.util.simpletimetracker.feature_change_record.adapter

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.utils.setData
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData as ViewData
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordPreviewItemBinding as Binding

fun createChangeRecordChangePreviewAdapterDelegate(
    onCheckboxClicked: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        containerChangeRecordPreviewItem.viewChangeRecordPreviewBefore.setData(item.before)
        containerChangeRecordPreviewItem.viewChangeRecordPreviewAfter.setData(item.after)
        containerChangeRecordPreviewItem.viewChangeRecordPreviewRemoved.isVisible = item.isRemoveVisible
        val alphaAfter = if (item.isRemoveVisible) 0.3f else 0.7f
        containerChangeRecordPreviewItem.viewChangeRecordPreviewAfter.alpha = alphaAfter
        if (checkChangeRecordPreviewItem.isChecked != item.isChecked) {
            checkChangeRecordPreviewItem.isChecked = item.isChecked
        }

        checkChangeRecordPreviewItem.setOnClick { onCheckboxClicked(item) }
    }
}