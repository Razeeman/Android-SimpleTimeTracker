package com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import kotlinx.android.synthetic.main.item_running_record_layout.view.*

class RunningRecordAdapterDelegate(
    private val onItemClick: ((RunningRecordViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordsViewHolder(parent)

    inner class RunningRecordsViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_running_record_layout) {

        override fun bind(item: ViewHolderType) = with(itemView) {
            item as RunningRecordViewData

            layoutRunningRecordItem.setCardBackgroundColor(item.color)
            ivRunningRecordItemIcon.setBackgroundResource(item.iconId)
            tvRunningRecordItemName.text = item.name
            tvRunningRecordItemTimeStarted.text = item.timeStarted
            tvRunningRecordItemTimer.text = item.timer

            layoutRunningRecordItem.setOnClickListener {
                onItemClick.invoke(item)
            }
        }
    }
}