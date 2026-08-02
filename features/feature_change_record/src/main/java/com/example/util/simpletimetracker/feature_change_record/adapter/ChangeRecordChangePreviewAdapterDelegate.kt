package com.example.util.simpletimetracker.feature_change_record.adapter

import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.utils.setData
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_change_record.api.viewData.ChangeRecordChangePreviewViewData as ViewData
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordPreviewItemBinding as Binding

fun createChangeRecordChangePreviewAdapterDelegate(
    onCheckboxClicked: (ViewData) -> Unit,
    onBeforeActionClicked: () -> Unit,
    onAfterActionClicked: () -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        root.setMargins(top = item.marginTopDp)
        ivChangeRecordPreviewCompare.isInvisible = !item.isCompareVisible
        checkChangeRecordPreviewItem.isVisible = item.isCheckVisible
        btnChangeRecordPreviewBeforeAction.isVisible = item.isBeforeActionVisible
        btnChangeRecordPreviewAfterAction.isVisible = item.isAfterActionVisible
        viewChangeRecordPreviewBefore.setData(item.before)
        viewChangeRecordPreviewAfter.setData(item.after)
        viewChangeRecordPreviewRemoved.isVisible = item.isRemoveVisible
        val alphaAfter = if (item.isRemoveVisible) 0.3f else 0.7f
        viewChangeRecordPreviewAfter.alpha = alphaAfter
        if (checkChangeRecordPreviewItem.isChecked != item.isChecked) {
            checkChangeRecordPreviewItem.isChecked = item.isChecked
        }

        checkChangeRecordPreviewItem.setOnClick { onCheckboxClicked(item) }
        btnChangeRecordPreviewBeforeAction.setOnClick { onBeforeActionClicked() }
        btnChangeRecordPreviewAfterAction.setOnClick { onAfterActionClicked() }
    }
}