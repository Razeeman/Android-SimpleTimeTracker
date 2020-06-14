package com.example.util.simpletimetracker.feature_running_records.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_running_records.R

class RunningRecordDividerAdapterDelegate() : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordDividerViewHolder(parent)

    inner class RunningRecordDividerViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_running_record_divider_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) {
            // Nothing to bind
        }
    }
}