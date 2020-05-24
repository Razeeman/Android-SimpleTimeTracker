package com.example.util.simpletimetracker.feature_statistics.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsEmptyViewData
import kotlinx.android.synthetic.main.item_statistics_empty_layout.view.*

class StatisticsEmptyAdapterDelegate() : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        StatisticsEmptyViewHolder(parent)

    inner class StatisticsEmptyViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_statistics_empty_layout) {

        override fun bind(item: ViewHolderType) = with(itemView) {
            item as StatisticsEmptyViewData

            tvStatisticsEmptyItem.text = item.message
        }
    }
}