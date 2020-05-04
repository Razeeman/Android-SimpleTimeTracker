package com.example.util.simpletimetracker.feature_running_records.adapter

import com.example.util.simpletimetracker.core.di.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.di.adapter.ViewHolderType

class RunningRecordsAdapter : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = RunningRecordAdapterDelegate()
    }
}