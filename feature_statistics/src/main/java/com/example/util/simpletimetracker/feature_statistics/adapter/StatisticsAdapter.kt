package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate

class StatisticsAdapter(
    onFilterClick: (() -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.HEADER] = StatisticsChartAdapterDelegate(onFilterClick)
        delegates[ViewHolderType.VIEW] = StatisticsAdapterDelegate()
        delegates[ViewHolderType.EMPTY] = StatisticsEmptyAdapterDelegate(onFilterClick)
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
    }
}