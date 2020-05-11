package com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData

class RunningRecordAdapter(
    onItemClick: ((RunningRecordViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = RunningRecordAdapterDelegate(onItemClick)
        delegates[ViewHolderType.HEADER] = RunningRecordEmptyAdapterDelegate()
    }
}