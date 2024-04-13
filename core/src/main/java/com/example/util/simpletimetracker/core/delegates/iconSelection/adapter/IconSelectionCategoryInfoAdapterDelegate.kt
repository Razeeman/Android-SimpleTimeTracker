package com.example.util.simpletimetracker.core.delegates.iconSelection.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.databinding.ItemIconSelectionCategoryInfoLayoutBinding as Binding
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionCategoryInfoViewData as ViewData

fun createIconSelectionCategoryInfoAdapterDelegate() =
    createRecyclerBindingAdapterDelegate<ViewData, Binding>(
        Binding::inflate,
    ) { binding, item, _ ->

        with(binding) {
            item as ViewData

            tvIconSelectionCategoryInfoItem.text = item.text
        }
    }