package com.example.util.simpletimetracker.feature_running_records.adapter

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.dpToPx
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.extension.setOnLongClick
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.R
import kotlinx.android.synthetic.main.item_running_record_type_layout.view.viewRecordTypeItem

fun createRunningRecordTypeAdapterDelegate(
    onItemClick: ((RecordTypeViewData) -> Unit),
    onItemLongClick: ((RecordTypeViewData, Map<Any, String>) -> Unit)
) = createRecyclerAdapterDelegate<RecordTypeViewData>(
    R.layout.item_running_record_type_layout
) { itemView, item, _ ->

    with(itemView.viewRecordTypeItem) {
        item as RecordTypeViewData
        val transitionName = TransitionNames.RECORD_TYPE + item.id

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
        setOnClickWith(item, onItemClick)
        setOnLongClick { onItemLongClick(item, mapOf(this to transitionName)) }
        ViewCompat.setTransitionName(this, transitionName)
    }
}