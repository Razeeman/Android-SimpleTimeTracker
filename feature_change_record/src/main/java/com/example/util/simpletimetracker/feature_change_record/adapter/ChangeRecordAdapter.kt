package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData

class ChangeRecordAdapter(
    onItemClick: ((RecordTypeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.RECORD_TYPE] = ChangeRecordAdapterDelegate(onItemClick)
    }
}