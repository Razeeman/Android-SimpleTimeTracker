package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryViewData
import kotlinx.android.synthetic.main.change_record_type_item_icon_category_layout.view.tvChangeRecordTypeIconCategoryItem

fun createChangeRecordTypeIconCategoryAdapterDelegate(
    onItemClick: ((ChangeRecordTypeIconCategoryViewData) -> Unit)
) = createRecyclerAdapterDelegate<ChangeRecordTypeIconCategoryViewData>(
    R.layout.change_record_type_item_icon_category_layout
) { itemView, item, _ ->

    with(itemView) {
        item as ChangeRecordTypeIconCategoryViewData
        tvChangeRecordTypeIconCategoryItem.setImageResource(item.categoryIcon)
        setOnClickWith(item, onItemClick)
    }
}