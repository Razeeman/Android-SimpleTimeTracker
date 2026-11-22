package com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemStatisticsGoalLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.StatisticsGoalViewData as ViewData

fun createStatisticsGoalAdapterDelegate(
    onItemClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.viewStatisticsGoalItem) {
        item as ViewData

        itemColor = item.color
        itemName = item.name
        itemEstimate = item.goal.estimate
        itemGoalCurrent = item.goal.goalCurrent
        itemGoal = item.goal.goal
        itemGoalPercent = item.goal.goalPercent
        itemGoalState = item.goal.goalState

        if (item.icon != null) {
            itemIconVisible = true
            itemIcon = item.icon
        } else {
            itemIconVisible = false
        }

        setOnClickWith(item, onItemClick)
    }
}
