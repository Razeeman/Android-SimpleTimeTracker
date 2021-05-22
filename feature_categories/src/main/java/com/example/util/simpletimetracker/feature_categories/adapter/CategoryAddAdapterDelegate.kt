package com.example.util.simpletimetracker.feature_categories.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_categories.viewData.CategoryAddViewData
import kotlinx.android.synthetic.main.categories_item_category_layout.view.*

fun createCategoryAddAdapterDelegate(
    onItemClick: ((CategoryAddViewData) -> Unit)
) = createRecyclerAdapterDelegate<CategoryAddViewData>(
    R.layout.categories_item_category_layout
) { itemView, item, _ ->

    with(itemView.viewCategoryItem) {
        item as CategoryAddViewData

        itemColor = item.color
        itemName = item.name
        itemIconVisible = false
        setOnClickWith(item, onItemClick)
    }
}