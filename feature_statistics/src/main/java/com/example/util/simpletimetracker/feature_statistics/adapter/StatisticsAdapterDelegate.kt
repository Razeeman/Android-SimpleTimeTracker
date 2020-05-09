package com.example.util.simpletimetracker.feature_statistics.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics.R
import kotlinx.android.synthetic.main.statistics_item_layout.view.*

class StatisticsAdapterDelegate() : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordsViewHolder(parent)

    inner class RunningRecordsViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.statistics_item_layout) {

        override fun bind(item: ViewHolderType) = with(itemView) {
            item as StatisticsViewData

            layoutStatisticsItem.setCardBackgroundColor(item.color)
            ivStatisticsItemIcon.setBackgroundResource(item.iconId)
            tvStatisticsItemName.text = item.name
            tvStatisticsItemDuration.text = item.duration
        }
    }
}