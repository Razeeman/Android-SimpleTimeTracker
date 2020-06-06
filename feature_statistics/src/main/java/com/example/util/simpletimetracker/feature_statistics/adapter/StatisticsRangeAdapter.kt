package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsRangeViewData

class StatisticsRangeAdapter(
    onRangeClick: ((StatisticsRangeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = StatisticsRangeAdapterDelegate(onRangeClick)
    }
}