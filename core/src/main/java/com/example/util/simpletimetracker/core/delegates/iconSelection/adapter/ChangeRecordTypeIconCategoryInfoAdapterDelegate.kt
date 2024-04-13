package com.example.util.simpletimetracker.core.delegates.iconSelection.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.databinding.ChangeRecordTypeItemIconCategoryInfoLayoutBinding as Binding
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconCategoryInfoViewData as ViewData

fun createChangeRecordTypeIconCategoryInfoAdapterDelegate() =
    createRecyclerBindingAdapterDelegate<ViewData, Binding>(
        Binding::inflate,
    ) { binding, item, _ ->

        with(binding) {
            item as ViewData

            tvChangeRecordTypeIconCategoryInfoItem.text = item.text
        }
    }