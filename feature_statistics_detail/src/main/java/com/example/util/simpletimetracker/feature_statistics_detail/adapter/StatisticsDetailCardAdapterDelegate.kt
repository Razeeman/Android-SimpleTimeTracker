package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import android.util.TypedValue
import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData
import kotlinx.android.synthetic.main.statistics_detail_card_item.view.*

class StatisticsDetailCardAdapterDelegate(
    private val titleTextSize: Int
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        StatisticsCardViewHolder(parent)

    inner class StatisticsCardViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.statistics_detail_card_item) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as StatisticsDetailCardViewData

            tvStatisticsDetailCardValue.text = item.title
            tvStatisticsDetailCardValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize.toFloat())
            tvStatisticsDetailCardDescription.text = item.subtitle
        }
    }
}