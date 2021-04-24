package com.example.util.simpletimetracker.feature_widget.configure.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.feature_widget.R
import kotlinx.android.synthetic.main.item_widget_record_type_layout.view.viewRecordTypeItem

fun createWidgetAdapterDelegate(
    onItemClick: ((RecordTypeViewData) -> Unit)
) = createRecyclerAdapterDelegate<RecordTypeViewData>(
    R.layout.item_widget_record_type_layout
) { itemView, item, _ ->

    with(itemView.viewRecordTypeItem) {
        item as RecordTypeViewData

        itemColor = item.color
        itemIcon = item.iconId
        itemIconColor = item.iconColor
        itemIconAlpha = item.iconAlpha
        itemName = item.name
        setOnClickWith(item, onItemClick)
    }
}