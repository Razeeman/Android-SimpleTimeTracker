package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class StatisticsDetailCardAdapter(
    titleTextSize: Int
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = StatisticsDetailCardAdapterDelegate(titleTextSize)
    }
}