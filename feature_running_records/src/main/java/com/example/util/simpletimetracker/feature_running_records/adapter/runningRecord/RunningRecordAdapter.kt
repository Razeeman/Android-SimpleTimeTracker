package com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.adapter.recordType.RecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.adapter.recordType.RecordTypeAddAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData

class RunningRecordAdapter(
    onItemClick: ((RunningRecordViewData) -> Unit),
    onTypeClick: ((RecordTypeViewData) -> Unit),
    onTypeLongClick: ((RecordTypeViewData) -> Unit),
    onAddClick: (() -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = RunningRecordAdapterDelegate(onItemClick)
        delegates[ViewHolderType.DIVIDER] = RunningRecordDividerAdapterDelegate()
        delegates[ViewHolderType.VIEW2] = RecordTypeAdapterDelegate(onTypeClick, onTypeLongClick)
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
        delegates[ViewHolderType.EMPTY] = EmptyAdapterDelegate()
        delegates[ViewHolderType.FOOTER] = RecordTypeAddAdapterDelegate(onAddClick)
    }
}