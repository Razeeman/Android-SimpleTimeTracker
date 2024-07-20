package com.example.util.simpletimetracker.feature_change_record.adapter

import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.utils.setData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSimpleViewData
import com.example.util.simpletimetracker.feature_views.extension.setMargins
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

        root.setMargins(top = item.marginTopDp)
        ivChangeRecordPreviewCompare.isInvisible = !item.isCompareVisible
        checkChangeRecordPreviewItem.isVisible = item.isCheckVisible
        viewChangeRecordPreviewBefore.setData(item.before)
        viewChangeRecordPreviewAfter.setData(item.after)
        viewChangeRecordPreviewRemoved.isVisible = item.isRemoveVisible
        val alphaAfter = if (item.isRemoveVisible) 0.3f else 0.7f
        viewChangeRecordPreviewAfter.alpha = alphaAfter
        if (checkChangeRecordPreviewItem.isChecked != item.isChecked) {
            checkChangeRecordPreviewItem.isChecked = item.isChecked
        }

        checkChangeRecordPreviewItem.setOnClick { onCheckboxClicked(item) }
    }
}

data class ChangeRecordChangePreviewViewData(
    val id: Long,
    val before: ChangeRecordSimpleViewData,
    val after: ChangeRecordSimpleViewData,
    val isChecked: Boolean,
    val marginTopDp: Int,
    val isRemoveVisible: Boolean,
    val isCheckVisible: Boolean,
    val isCompareVisible: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData
}