package com.example.util.simpletimetracker.feature_statistics.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsRangeViewData
import kotlinx.android.synthetic.main.item_statistics_range_layout.view.*

class StatisticsRangeAdapterDelegate(
    private val onRangeClick: ((StatisticsRangeViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        StatisticsRangeViewHolder(parent)

    inner class StatisticsRangeViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_statistics_range_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as StatisticsRangeViewData

            btnStatisticsItemRange.text = item.name
            btnStatisticsItemRange.setOnClick {
                onRangeClick(item)
            }
        }
    }
}