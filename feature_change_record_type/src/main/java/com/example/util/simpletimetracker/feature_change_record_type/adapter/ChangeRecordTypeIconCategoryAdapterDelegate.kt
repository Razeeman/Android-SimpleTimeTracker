package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record_type.databinding.ChangeRecordTypeItemIconCategoryLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryViewData as ViewData

fun createChangeRecordTypeIconCategoryAdapterDelegate(
    onItemClick: ((ViewData) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData
        ivChangeRecordTypeIconCategoryItem.setImageResource(item.categoryIcon)
        ivChangeRecordTypeIconCategoryItem.tag = item.categoryIcon
        root.setOnClickWith(item, onItemClick)
    }
}