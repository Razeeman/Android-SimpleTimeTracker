package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsActivityViewData

class StatisticsAdapter(
    onFilterClick: (() -> Unit),
    onItemClick: ((StatisticsActivityViewData, Map<Any, String>) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.HEADER] = StatisticsChartAdapterDelegate(onFilterClick)
        delegates[ViewHolderType.VIEW] = StatisticsAdapterDelegate(onItemClick)
        delegates[ViewHolderType.EMPTY] = StatisticsEmptyAdapterDelegate(onFilterClick)
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
    }
}