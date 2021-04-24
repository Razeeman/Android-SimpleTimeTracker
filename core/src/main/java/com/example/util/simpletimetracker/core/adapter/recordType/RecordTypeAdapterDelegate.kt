package com.example.util.simpletimetracker.core.adapter.recordType

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.dpToPx
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import kotlinx.android.synthetic.main.item_record_type_layout.view.viewRecordTypeItem

fun createRecordTypeAdapterDelegate(
    onItemClick: ((RecordTypeViewData) -> Unit)? = null
) = createRecyclerAdapterDelegate<RecordTypeViewData>(
    R.layout.item_record_type_layout
) { itemView, item, _ ->

    with(itemView.viewRecordTypeItem) {
        item as RecordTypeViewData

        layoutParams = layoutParams.also { params ->
            item.width?.dpToPx()?.let { params.width = it }
            item.height?.dpToPx()?.let { params.height = it }
        }

        itemIsRow = item.asRow
        itemColor = item.color
        itemIcon = item.iconId
        itemIconColor = item.iconColor
        itemIconAlpha = item.iconAlpha
        itemName = item.name
        onItemClick?.let { setOnClickWith(item, onItemClick) }
    }
}