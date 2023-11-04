package com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal

import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemStatisticsGoalLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.StatisticsGoalViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate

fun createStatisticsGoalAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewStatisticsGoalItem) {
        item as ViewData

        itemColor = item.color
        itemName = item.name
        itemGoalCurrent = item.goal.goalCurrent
        itemGoal = item.goal.goal
        itemGoalPercent = item.goal.goalPercent
        itemGoalTimeComplete = item.goal.goalComplete

        if (item.icon != null) {
            itemIconVisible = true
            itemIcon = item.icon
        } else {
            itemIconVisible = false
        }
    }
}
