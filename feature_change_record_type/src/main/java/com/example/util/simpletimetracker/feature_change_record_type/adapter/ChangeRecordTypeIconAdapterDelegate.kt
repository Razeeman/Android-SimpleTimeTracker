package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import kotlinx.android.synthetic.main.change_record_type_item_icon_layout.view.ivChangeRecordTypeIconItem
import kotlinx.android.synthetic.main.change_record_type_item_icon_layout.view.layoutChangeRecordTypeIconItem

fun createChangeRecordTypeIconAdapterDelegate(
    onIconItemClick: ((ChangeRecordTypeIconViewData) -> Unit)
) = createRecyclerAdapterDelegate<ChangeRecordTypeIconViewData>(
    R.layout.change_record_type_item_icon_layout
) { itemView, item, _ ->

    with(itemView) {
        item as ChangeRecordTypeIconViewData

        layoutChangeRecordTypeIconItem.setCardBackgroundColor(item.colorInt)
        ivChangeRecordTypeIconItem.setBackgroundResource(item.iconResId)
        ivChangeRecordTypeIconItem.tag = item.iconResId
        layoutChangeRecordTypeIconItem.setOnClickWith(item, onIconItemClick)
    }
}