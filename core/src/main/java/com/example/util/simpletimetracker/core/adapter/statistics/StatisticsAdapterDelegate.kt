package com.example.util.simpletimetracker.core.adapter.statistics

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.view.TransitionNames
import kotlinx.android.synthetic.main.item_statistics_layout.view.*

fun createStatisticsAdapterDelegate(
    addTransitionNames: Boolean = false,
    onItemClick: ((StatisticsViewData, Map<Any, String>) -> Unit)
) = createRecyclerAdapterDelegate<StatisticsViewData>(
    R.layout.item_statistics_layout
) { itemView, item, _ ->

    with(itemView.viewStatisticsItem) {
        item as StatisticsViewData
        val transitionName = TransitionNames.STATISTICS_DETAIL + item.id

        itemColor = item.color
        itemName = item.name
        itemDuration = item.duration
        itemPercent = item.percent

        if (item is StatisticsViewData.Activity) {
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
