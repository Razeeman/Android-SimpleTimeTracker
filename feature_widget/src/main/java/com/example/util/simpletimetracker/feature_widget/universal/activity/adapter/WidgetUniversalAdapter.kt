package com.example.util.simpletimetracker.feature_widget.universal.activity.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.recordType.RecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData

class WidgetUniversalAdapter(
    onItemClick: ((RecordTypeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.RECORD_TYPE] = RecordTypeAdapterDelegate(onItemClick)
        delegates[ViewHolderType.EMPTY] = EmptyAdapterDelegate()
    }
}