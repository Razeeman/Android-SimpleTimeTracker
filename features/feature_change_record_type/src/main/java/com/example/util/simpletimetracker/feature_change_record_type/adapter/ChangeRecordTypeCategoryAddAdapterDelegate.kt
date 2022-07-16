package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_change_record_type.databinding.ChangeRecordTypeItemCategoryAddLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeCategoryAddViewData as ViewData

fun createChangeRecordTypeCategoryAddAdapterDelegate(
    onItemClick: (() -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.root) {
        item as ViewData
        setOnClick(onItemClick)
    }
}