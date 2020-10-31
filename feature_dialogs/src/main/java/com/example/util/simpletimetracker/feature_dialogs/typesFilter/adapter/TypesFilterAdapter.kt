package com.example.util.simpletimetracker.feature_dialogs.typesFilter.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.recordType.RecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData

class TypesFilterAdapter(
    onItemClick: ((RecordTypeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.RECORD_TYPE] = RecordTypeAdapterDelegate(onItemClick)
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
    }
}