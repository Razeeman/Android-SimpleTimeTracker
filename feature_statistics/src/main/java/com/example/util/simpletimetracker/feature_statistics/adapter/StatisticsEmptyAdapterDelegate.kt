package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_statistics.R
import kotlinx.android.synthetic.main.item_statistics_empty_layout.view.btnStatisticsEmptyFilter
import kotlinx.android.synthetic.main.item_statistics_empty_layout.view.tvEmptyItem

fun createStatisticsEmptyAdapterDelegate(
    onFilterClick: (() -> Unit)
) = createRecyclerAdapterDelegate<EmptyViewData>(
    R.layout.item_statistics_empty_layout
) { itemView, item, _ ->

    with(itemView) {
        item as EmptyViewData

        tvEmptyItem.text = item.message
        btnStatisticsEmptyFilter.setOnClick(onFilterClick)
    }
}