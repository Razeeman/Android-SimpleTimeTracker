package com.example.util.simpletimetracker.feature_dialogs.chartFilter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class ChartFilterAdapter(
    onItemClick: ((ChartFilterRecordTypeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = ChartFilterAdapterDelegate(onItemClick)
    }
}