package com.example.util.simpletimetracker.feature_statistics.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_statistics.R
import kotlinx.android.synthetic.main.item_statistics_empty_layout.view.*

class StatisticsEmptyAdapterDelegate(
    private val onFilterClick: (() -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        EmptyViewHolder(parent)

    inner class EmptyViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_statistics_empty_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as EmptyViewData

            tvEmptyItem.text = item.message
            btnStatisticsEmptyFilter.setOnClick(onFilterClick)
        }
    }
}