package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.CategoryViewData
import com.example.util.simpletimetracker.feature_change_record_type.R
import kotlinx.android.synthetic.main.change_record_type_item_category_layout.view.viewCategoryItem

fun createChangeRecordTypeCategoryAdapterDelegate(
    onItemClick: ((CategoryViewData) -> Unit)
) = createRecyclerAdapterDelegate<CategoryViewData>(
    R.layout.change_record_type_item_category_layout
) { itemView, item, _ ->

    with(itemView.viewCategoryItem) {
        item as CategoryViewData

        itemColor = item.color
        itemName = item.name
        itemTextColor = item.textColor
        setOnClickWith(item, onItemClick)
    }
}