package com.example.util.simpletimetracker.feature_base_adapter.category

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemCategoryLayoutBinding as Binding

fun createCategoryAdapterDelegate(
    onItemClick: ((ViewData) -> Unit)? = null
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewCategoryItem) {
        item as ViewData

        itemColor = item.color
        itemName = item.name
        itemIconColor = item.iconColor

        if (item is ViewData.Record) {
            itemIconAlpha = item.iconAlpha
            itemIconVisible = item.icon != null
            item.icon?.let(this::itemIcon::set)
        } else {
            itemIconVisible = false
        }

        onItemClick?.let { setOnClickWith(item, onItemClick) }
    }
}