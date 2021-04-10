package com.example.util.simpletimetracker.core.adapter.record

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.viewData.RecordViewData
import kotlinx.android.synthetic.main.item_record_layout.view.viewRecordItem

fun createRecordAdapterDelegate(
    onItemClick: ((RecordViewData, Map<Any, String>) -> Unit)
) = createRecyclerAdapterDelegate<RecordViewData>(
    R.layout.item_record_layout
) { itemView, item, _ ->

    with(itemView.viewRecordItem) {
        item as RecordViewData
        val transitionName = TransitionNames.RECORD + item.getUniqueId()

        itemColor = item.color
        itemIcon = item.iconId
        itemName = item.name
        itemTimeStarted = item.timeStarted
        itemTimeEnded = item.timeFinished
        itemDuration = item.duration
        itemComment = item.comment

        setOnClick { onItemClick(item, mapOf(this to transitionName)) }
        ViewCompat.setTransitionName(this, transitionName)
    }
}