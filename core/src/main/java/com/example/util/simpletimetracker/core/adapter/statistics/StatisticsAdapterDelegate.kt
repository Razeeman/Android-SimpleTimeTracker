package com.example.util.simpletimetracker.core.adapter.statistics

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.adapter.statistics.StatisticsViewData as ViewData
import com.example.util.simpletimetracker.core.databinding.ItemStatisticsLayoutBinding as Binding

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
