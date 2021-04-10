package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsInfoViewData
import kotlinx.android.synthetic.main.item_statistics_info_layout.view.tvStatisticsInfoName
import kotlinx.android.synthetic.main.item_statistics_info_layout.view.tvStatisticsInfoText

fun createStatisticsInfoAdapterDelegate() = createRecyclerAdapterDelegate<StatisticsInfoViewData>(
    R.layout.item_statistics_info_layout
) { itemView, item, _ ->

    with(itemView) {
        item as StatisticsInfoViewData

        tvStatisticsInfoName.text = item.name
        tvStatisticsInfoText.text = item.text
    }
}