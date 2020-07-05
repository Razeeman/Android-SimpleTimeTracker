package com.example.util.simpletimetracker.feature_change_running_record.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData

class ChangeRunningRecordAdapter(
    onItemClick: ((RecordTypeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.RECORD_TYPE] = ChangeRunningRecordAdapterDelegate(onItemClick)
    }
}