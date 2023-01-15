package com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal

import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemStatisticsGoalLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.StatisticsGoalViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate

fun createStatisticsGoalAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewStatisticsItem) {
        item as ViewData

        itemColor = item.color
        itemName = item.name
        itemIcon = item.icon
        itemGoalTime = item.goal.goalTime
        itemGoalPercent = item.goal.goalPercent
        itemGoalTimeComplete = item.goal.goalComplete
    }
}
