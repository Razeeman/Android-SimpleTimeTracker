package com.example.util.simpletimetracker.feature_running_records.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.dpToPx
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordTypeAddViewData
import kotlinx.android.synthetic.main.item_running_record_type_layout.view.viewRecordTypeItem

fun createRunningRecordTypeAddAdapterDelegate(
    onItemClick: ((RunningRecordTypeAddViewData) -> Unit)
) = createRecyclerAdapterDelegate<RunningRecordTypeAddViewData>(
    R.layout.item_running_record_type_layout
) { itemView, item, _ ->

    with(itemView.viewRecordTypeItem) {
        item as RunningRecordTypeAddViewData

        layoutParams = layoutParams.also { params ->
            params.width = item.width.dpToPx()
            params.height = item.height.dpToPx()
        }

        itemIsRow = item.asRow
        itemColor = item.color
        itemIcon = item.iconId
        itemName = item.name
        setOnClickWith(item, onItemClick)
    }
}