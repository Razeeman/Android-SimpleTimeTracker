package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryInfoViewData
import kotlinx.android.synthetic.main.change_record_type_item_icon_category_info_layout.view.tvChangeRecordTypeIconCategoryInfoItem

fun createChangeRecordTypeIconCategoryInfoAdapterDelegate() =
    createRecyclerAdapterDelegate<ChangeRecordTypeIconCategoryInfoViewData>(
        R.layout.change_record_type_item_icon_category_info_layout
    ) { itemView, item, _ ->

        with(itemView) {
            item as ChangeRecordTypeIconCategoryInfoViewData

            tvChangeRecordTypeIconCategoryInfoItem.text = item.text
        }
    }