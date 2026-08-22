package com.example.util.simpletimetracker.feature_base_adapter.record

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.RecordView
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRecordLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData as ViewData

fun createRecordAdapterDelegate(
    onItemClick: ((ViewData, Pair<Any, String>) -> Unit) = { _, _ -> },
    onItemLongClick: ((ViewData, Pair<Any, String>) -> Unit) = { _, _ -> },
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.viewRecordItem) {
        item as ViewData
        val transitionName = TransitionNames.RECORD + item.getUniqueId()

        bindState(item)
        setOnClick { onItemClick(item, this to transitionName) }
        setOnLongClick { onItemLongClick(item, this to transitionName) }
        ViewCompat.setTransitionName(this, transitionName)
    }
}

fun RecordView.bindState(item: ViewData) {
    itemColor = item.color
    itemIcon = item.iconId
    itemName = item.name
    itemTagName = item.tagName
    itemTimeStarted = item.timeStarted
    itemTimeEnded = item.timeFinished
    itemDuration = item.duration
    itemDurationTotal = item.durationTotal
    itemComment = item.comment
}