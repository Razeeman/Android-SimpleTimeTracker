package com.example.util.simpletimetracker.feature_categories.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemCategoryLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_categories.viewData.CategoryAddViewData as ViewData

fun createCategoryAddAdapterDelegate(
    onItemClick: ((ViewData) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewCategoryItem) {
        item as ViewData

        itemColor = item.color
        itemName = item.name
        itemIconVisible = false
        setOnClickWith(item, onItemClick)
    }
}