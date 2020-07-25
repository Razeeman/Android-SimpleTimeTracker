package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData

class StatisticsDetailLengthAdapter(
    onRangeClick: ((StatisticsDetailChartLengthViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = StatisticsDetailLengthAdapterDelegate(onRangeClick)
    }
}