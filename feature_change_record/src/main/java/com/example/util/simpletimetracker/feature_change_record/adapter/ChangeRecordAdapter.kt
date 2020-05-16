package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordTypeViewData

class ChangeRecordAdapter(
    onItemClick: ((ChangeRecordTypeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = ChangeRecordAdapterDelegate(onItemClick)
    }
}