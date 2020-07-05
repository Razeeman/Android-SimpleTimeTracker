package com.example.util.simpletimetracker.feature_running_records.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import kotlinx.android.synthetic.main.item_running_record_layout.view.*

class RunningRecordAdapterDelegate(
    private val onItemClick: ((RunningRecordViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordViewHolder(parent)

    inner class RunningRecordViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_running_record_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as RunningRecordViewData

            val rebind: Boolean = payloads.isEmpty() || payloads.first() !is List<*>
            val updates = (payloads.firstOrNull() as? List<*>) ?: emptyList<Int>()

            if (rebind || updates.contains(RunningRecordViewData.UPDATE_NAME).orFalse()) {
                viewRunningRecordItem.itemName = item.name
            }
            if (rebind || updates.contains(RunningRecordViewData.UPDATE_TIME_STARTED).orFalse()) {
                viewRunningRecordItem.itemTimeStarted = item.timeStarted
            }
            if (rebind || updates.contains(RunningRecordViewData.UPDATE_TIMER).orFalse()) {
                viewRunningRecordItem.itemTimer = item.timer
            }
            if (rebind || updates.contains(RunningRecordViewData.UPDATE_ICON).orFalse()) {
                viewRunningRecordItem.itemIcon = item.iconId
            }
            if (rebind || updates.contains(RunningRecordViewData.UPDATE_COLOR).orFalse()) {
                viewRunningRecordItem.itemColor= item.color
            }
            if (rebind) {
                viewRunningRecordItem.setOnClickWith(item, onItemClick)
            }
        }
    }
}