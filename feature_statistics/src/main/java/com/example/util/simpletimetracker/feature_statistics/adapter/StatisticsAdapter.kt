package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class StatisticsAdapter() : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.HEADER] = StatisticsChartAdapterDelegate()
        delegates[ViewHolderType.VIEW] = StatisticsAdapterDelegate()
    }
}