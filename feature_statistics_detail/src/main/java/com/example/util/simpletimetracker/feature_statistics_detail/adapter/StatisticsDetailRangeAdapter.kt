package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailRangeViewData

class StatisticsDetailRangeAdapter(
    onRangeClick: ((StatisticsDetailRangeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = StatisticsDetailRangeAdapterDelegate(onRangeClick)
    }
}