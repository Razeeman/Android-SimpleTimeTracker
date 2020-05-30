package com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_running_records.R

class RunningRecordEmptyAdapterDelegate() : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordEmptyViewHolder(parent)

    inner class RunningRecordEmptyViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_running_record_empty_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) {
            // Nothing to bind
        }
    }
}