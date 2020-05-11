package com.example.util.simpletimetracker.feature_running_records.adapter.recordType

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_running_records.viewData.RecordTypeViewData

class RecordTypeAdapter(
    onItemClick: ((RecordTypeViewData) -> Unit),
    onItemLongClick: ((RecordTypeViewData) -> Unit),
    onAddClick: (() -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = RecordTypeAdapterDelegate(onItemClick, onItemLongClick)
        delegates[ViewHolderType.FOOTER] = RecordTypeAddAdapterDelegate(onAddClick)
    }
}