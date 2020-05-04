package com.example.util.simpletimetracker.feature_running_records.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.di.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.di.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.di.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_running_records.R
import kotlinx.android.synthetic.main.item_layout.view.*

class RunningRecordAdapterDelegate : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordsViewHolder(parent)

    inner class RunningRecordsViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_layout) {

        override fun bind(item: ViewHolderType) = with(itemView) {
            item as RunningRecordViewData

            tvItemId.text = item.id.toString()
            tvItemName.text = item.name
            tvItemTimeStarted.text = item.timeStarted.toString()
            tvItemTimeEnded.text = item.timeEnded.toString()
        }
    }
}