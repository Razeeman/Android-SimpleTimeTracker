package com.example.util.simpletimetracker.feature_change_category.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.recordType.RecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData

class ChangeCategoryTypeAdapter(
    onItemClick: ((RecordTypeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.RECORD_TYPE] = RecordTypeAdapterDelegate(onItemClick)
    }
}