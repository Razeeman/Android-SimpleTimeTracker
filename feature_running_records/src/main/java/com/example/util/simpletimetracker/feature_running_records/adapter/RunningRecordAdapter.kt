package com.example.util.simpletimetracker.feature_running_records.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData

class RunningRecordAdapter(
    onRecordClick: ((RunningRecordViewData) -> Unit),
    onTypeClick: ((RecordTypeViewData) -> Unit),
    onTypeLongClick: ((RecordTypeViewData) -> Unit),
    onAddClick: (() -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = RunningRecordAdapterDelegate(onRecordClick)
        delegates[ViewHolderType.DIVIDER] = RunningRecordDividerAdapterDelegate()
        delegates[ViewHolderType.VIEW2] = RunningRecordTypeAdapterDelegate(onTypeClick, onTypeLongClick)
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
        delegates[ViewHolderType.EMPTY] = EmptyAdapterDelegate()
        delegates[ViewHolderType.FOOTER] = RunningRecordTypeAddAdapterDelegate(onAddClick)
    }
}