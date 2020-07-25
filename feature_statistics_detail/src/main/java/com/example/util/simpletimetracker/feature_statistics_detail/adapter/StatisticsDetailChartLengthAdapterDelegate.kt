package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import android.content.res.ColorStateList
import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import kotlinx.android.synthetic.main.item_statistics_detail__chart_length_layout.view.*

class StatisticsDetailChartLengthAdapterDelegate(
    private val onRangeClick: ((StatisticsDetailChartLengthViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        StatisticsRangeViewHolder(parent)

    inner class StatisticsRangeViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_statistics_detail__chart_length_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as StatisticsDetailChartLengthViewData

            btnStatisticsDetailItemLength.text = item.name
            btnStatisticsDetailItemLength.backgroundTintList = ColorStateList.valueOf(item.color)
            btnStatisticsDetailItemLength.setOnClickWith(item, onRangeClick)
        }
    }
}