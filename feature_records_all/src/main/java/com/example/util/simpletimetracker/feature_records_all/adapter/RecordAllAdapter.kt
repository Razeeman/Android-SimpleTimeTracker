package com.example.util.simpletimetracker.feature_records_all.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.record.RecordAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.RecordViewData

class RecordAllAdapter(
    onItemLongClick: ((RecordViewData, Map<Any, String>) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.RECORD] = RecordAdapterDelegate(onItemLongClick)
        delegates[ViewHolderType.EMPTY] = EmptyAdapterDelegate()
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
    }
}