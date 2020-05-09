package com.example.util.simpletimetracker.feature_records.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_records.R
import kotlinx.android.synthetic.main.record_item_layout.view.*

class RecordAdapterDelegate(
    private val onItemClick: ((RecordViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordsViewHolder(parent)

    inner class RunningRecordsViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.record_item_layout) {

        override fun bind(item: ViewHolderType) = with(itemView) {
            item as RecordViewData

            layoutRecordItem.setCardBackgroundColor(item.color)
            ivRecordItemIcon.setBackgroundResource(item.iconId)
            tvRecordItemName.text = item.name
            tvRecordItemTimeStarted.text = item.timeStarted
            tvRecordItemTimeFinished.text = item.timeFinished
            tvRecordItemDuration.text = item.duration

            layoutRecordItem.setOnClickListener {
                onItemClick.invoke(item)
            }
        }
    }
}