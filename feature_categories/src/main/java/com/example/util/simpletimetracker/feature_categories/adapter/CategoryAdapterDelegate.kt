package com.example.util.simpletimetracker.feature_categories.adapter

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.feature_categories.R
import kotlinx.android.synthetic.main.categories_item_category_layout.view.viewCategoryItem

fun createCategoryAdapterDelegate(
    onItemLongClick: ((CategoryViewData, Map<Any, String>) -> Unit)
) = createRecyclerAdapterDelegate<CategoryViewData>(
    R.layout.categories_item_category_layout
) { itemView, item, _ ->

    with(itemView.viewCategoryItem) {
        item as CategoryViewData

        val transitionName = when (item) {
            is CategoryViewData.Activity -> TransitionNames.ACTIVITY_TAG
            is CategoryViewData.Record -> TransitionNames.RECORD_TAG
            else -> ""
        } + item.id

        itemColor = item.color
        itemName = item.name
        itemIconColor = item.iconColor

        if (item is CategoryViewData.Record) {
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