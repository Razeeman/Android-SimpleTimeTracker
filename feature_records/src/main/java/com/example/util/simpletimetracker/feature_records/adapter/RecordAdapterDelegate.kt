package com.example.util.simpletimetracker.feature_records.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.viewData.RecordViewData
import kotlinx.android.synthetic.main.item_record_layout.view.*

class RecordAdapterDelegate(
    private val onItemLongClick: ((RecordViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RecordViewHolder(parent)

    inner class RecordViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_record_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView.viewRecordItem) {
            item as RecordViewData

            itemColor = item.color
            itemIcon = item.iconId
            itemName = item.name
            itemTimeStarted = item.timeStarted
            itemTimeEnded = item.timeFinished
            itemDuration = item.duration

            setOnLongClickListener {
                onItemLongClick.invoke(item)
                true
            }
        }
    }
}