package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import android.content.res.ColorStateList
import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import kotlinx.android.synthetic.main.item_statistics_detail_grouping_layout.view.*

class StatisticsDetailGroupingAdapterDelegate(
    private val onGroupingClick: ((StatisticsDetailGroupingViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        StatisticsGroupingViewHolder(parent)

    inner class StatisticsGroupingViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_statistics_detail_grouping_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as StatisticsDetailGroupingViewData

            btnStatisticsDetailItemGrouping.text = item.name
            btnStatisticsDetailItemGrouping.backgroundTintList = ColorStateList.valueOf(item.color)
            btnStatisticsDetailItemGrouping.setOnClickWith(item, onGroupingClick)
        }
    }
}