package com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_running_records.R
import kotlinx.android.synthetic.main.running_record_item_layout.view.*

class RunningRecordAdapterDelegate(
    private val onItemClick: ((RunningRecordViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordsViewHolder(parent)

    inner class RunningRecordsViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.running_record_item_layout) {

        override fun bind(item: ViewHolderType) = with(itemView) {
            item as RunningRecordViewData

            tvRunningRecordItemId.text = item.id.toString()
            tvRunningRecordItemName.text = item.name
            tvRunningRecordItemTime.text = item.timeString

            layoutRunningRecordItem.setOnClickListener {
                onItemClick.invoke(item)
            }
        }
    }
}