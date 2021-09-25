package com.example.util.simpletimetracker.feature_running_records.adapter

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_running_records.databinding.ItemRunningRecordLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData as ViewData

fun createRunningRecordAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
    onItemLongClick: ((ViewData, Map<Any, String>) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, payloads ->

    with(binding.viewRunningRecordItem) {
        item as ViewData
        val transitionName = TransitionNames.RECORD_RUNNING + item.id

        val rebind: Boolean = payloads.isEmpty() || payloads.first() !is List<*>
        val updates = (payloads.firstOrNull() as? List<*>) ?: emptyList<Int>()

        if (rebind || updates.contains(ViewData.UPDATE_NAME).orFalse()) {
            itemName = item.name
        }
        if (rebind || updates.contains(ViewData.UPDATE_TAG_NAME).orFalse()) {
            itemTagName = item.tagName
        }
        if (rebind || updates.contains(ViewData.UPDATE_TIME_STARTED).orFalse()) {
            itemTimeStarted = item.timeStarted
        }
        if (rebind || updates.contains(ViewData.UPDATE_TIMER).orFalse()) {
            itemTimer = item.timer
        }
        if (rebind || updates.contains(ViewData.UPDATE_GOAL_TIME).orFalse()) {
            itemGoalTime = item.goalTime
        }
        if (rebind || updates.contains(ViewData.UPDATE_ICON).orFalse()) {
            itemIcon = item.iconId
        }
        if (rebind || updates.contains(ViewData.UPDATE_COLOR).orFalse()) {
            itemColor = item.color
        }
        if (rebind || updates.contains(ViewData.UPDATE_COMMENT).orFalse()) {
            itemComment = item.comment
        }
        if (rebind) {
            setOnClickWith(item, onItemClick)
            setOnLongClick { onItemLongClick(item, mapOf(this to transitionName)) }
            ViewCompat.setTransitionName(this, transitionName)
        }
    }
}