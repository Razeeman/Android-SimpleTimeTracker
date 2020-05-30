package com.example.util.simpletimetracker.feature_statistics.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData
import kotlinx.android.synthetic.main.item_statistics_chart_layout.view.*

class StatisticsChartAdapterDelegate() : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        StatisticsChartViewHolder(parent)

    inner class StatisticsChartViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_statistics_chart_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as StatisticsChartViewData

            chartStatisticsItem.setSegments(item.data)
        }
    }
}