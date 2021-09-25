package com.example.util.simpletimetracker.core.adapter.record

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.databinding.ItemRecordLayoutBinding as Binding
import com.example.util.simpletimetracker.core.viewData.RecordViewData as ViewData

fun createRecordAdapterDelegate(
    onItemClick: ((ViewData, Map<Any, String>) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewRecordItem) {
        item as ViewData
        val transitionName = TransitionNames.RECORD + item.getUniqueId()

        itemColor = item.color
        itemIcon = item.iconId
        itemName = item.name
        itemTagName = item.tagName
        itemTimeStarted = item.timeStarted
        itemTimeEnded = item.timeFinished
        itemDuration = item.duration
        itemComment = item.comment

        setOnClick { onItemClick(item, mapOf(this to transitionName)) }
        ViewCompat.setTransitionName(this, transitionName)
    }
}