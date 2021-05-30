package com.example.util.simpletimetracker.core.adapter.category

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import kotlinx.android.synthetic.main.item_category_layout.view.viewCategoryItem

fun createCategoryAdapterDelegate(
    onItemClick: ((CategoryViewData) -> Unit)? = null
) = createRecyclerAdapterDelegate<CategoryViewData>(
    R.layout.item_category_layout
) { itemView, item, _ ->

    with(itemView.viewCategoryItem) {
        item as CategoryViewData

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

        onItemClick?.let { setOnClickWith(item, onItemClick) }
    }
}