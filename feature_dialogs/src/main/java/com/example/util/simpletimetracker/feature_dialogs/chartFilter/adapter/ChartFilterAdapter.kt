package com.example.util.simpletimetracker.feature_dialogs.chartFilter.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewData.ChartFilterRecordTypeViewData

class ChartFilterAdapter(
    onItemClick: ((ChartFilterRecordTypeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = ChartFilterAdapterDelegate(onItemClick)
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
    }
}