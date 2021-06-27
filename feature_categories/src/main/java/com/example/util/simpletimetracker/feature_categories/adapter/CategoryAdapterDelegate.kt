package com.example.util.simpletimetracker.feature_categories.adapter

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.adapter.category.CategoryViewData as ViewData
import com.example.util.simpletimetracker.feature_categories.databinding.CategoriesItemCategoryLayoutBinding as Binding

fun createCategoryAdapterDelegate(
    onItemLongClick: ((ViewData, Map<Any, String>) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewCategoryItem) {
        item as ViewData

        val transitionName = when (item) {
            is ViewData.Activity -> TransitionNames.ACTIVITY_TAG
            is ViewData.Record -> TransitionNames.RECORD_TAG
            else -> ""
        } + item.id

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

        setOnClick { onItemLongClick(item, mapOf(this to transitionName)) }
        ViewCompat.setTransitionName(this, transitionName)
    }
}