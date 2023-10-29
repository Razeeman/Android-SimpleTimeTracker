package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record_type.databinding.ChangeRecordTypeItemIconCategoryLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryViewData as ViewData
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_change_record_type.R

fun createChangeRecordTypeIconCategoryAdapterDelegate(
    onItemClick: ((ViewData) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData
        val tint = root.context.getThemedAttr(
            if (item.selected) R.attr.appLightTextColor else R.attr.colorPrimary
        )
        with(ivChangeRecordTypeIconCategoryItem) {
            setImageResource(item.categoryIcon)
            setColorFilter(tint)
            tag = item.categoryIcon
        }
        viewChangeRecordTypeIconCategoryItem.isVisible = item.selected
        root.setOnClickWith(item, onItemClick)
    }
}