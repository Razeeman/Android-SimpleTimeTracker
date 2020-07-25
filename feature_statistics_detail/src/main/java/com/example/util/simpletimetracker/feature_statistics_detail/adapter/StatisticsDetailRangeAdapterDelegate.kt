package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import android.content.res.ColorStateList
import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailRangeViewData
import kotlinx.android.synthetic.main.item_statistics_detail_range_layout.view.*

class StatisticsDetailRangeAdapterDelegate(
    private val onRangeClick: ((StatisticsDetailRangeViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        StatisticsRangeViewHolder(parent)

    inner class StatisticsRangeViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_statistics_detail_range_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as StatisticsDetailRangeViewData

            btnStatisticsDetailItemRange.text = item.name
            btnStatisticsDetailItemRange.backgroundTintList = ColorStateList.valueOf(item.color)
            btnStatisticsDetailItemRange.setOnClickWith(item, onRangeClick)
        }
    }
}