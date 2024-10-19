package com.example.util.simpletimetracker.feature_base_adapter.recordWithHint

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRecordWithHintLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.recordWithHint.RecordWithHintViewData as ViewData

fun createRecordWithHintAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.viewRecordItem) {
        item as ViewData

        item.record.let { item ->
            itemColor = item.color
            itemIcon = item.iconId
            itemName = item.name
            itemTagName = item.tagName
            itemTimeStarted = item.timeStarted
            itemTimeEnded = item.timeFinished
            itemDuration = item.duration
            itemComment = item.comment
        }
    }
}