package com.example.util.simpletimetracker.feature_base_adapter.statistics

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemStatisticsLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData as ViewData

fun createStatisticsAdapterDelegate(
    addTransitionNames: Boolean = false,
    onItemClick: ((ViewData, Map<Any, String>) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewStatisticsItem) {
        item as ViewData
        val transitionName = TransitionNames.STATISTICS_DETAIL + item.id

        itemColor = item.color
        itemName = item.name
        itemDuration = item.duration
        itemPercent = item.percent

        if (item is ViewData.Activity) {
            itemIconVisible = true
            itemIcon = item.icon
        } else {
            itemIconVisible = false
        }

        setOnClick { onItemClick(item, mapOf(this to transitionName)) }
        if (addTransitionNames) {
            ViewCompat.setTransitionName(this, transitionName)
        }
    }
}
