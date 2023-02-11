package com.example.util.simpletimetracker.feature_base_adapter.runningRecord

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRunningRecordLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData as ViewData

fun createRunningRecordAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
    onItemLongClick: ((ViewData, Map<Any, String>) -> Unit) = { _, _ -> },
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
            itemGoalTime = item.goalTime.text
            itemGoalTimeComplete = item.goalTime.complete
        }
        if (rebind || updates.contains(ViewData.UPDATE_GOAL_TIME2).orFalse()) {
            itemGoalTime2 = item.goalTime2.text
            itemGoalTime2Complete = item.goalTime2.complete
        }
        if (rebind || updates.contains(ViewData.UPDATE_GOAL_TIME3).orFalse()) {
            itemGoalTime3 = item.goalTime3.text
            itemGoalTime3Complete = item.goalTime3.complete
        }
        if (rebind || updates.contains(ViewData.UPDATE_GOAL_TIME4).orFalse()) {
            itemGoalTime4 = item.goalTime4.text
            itemGoalTime4Complete = item.goalTime4.complete
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