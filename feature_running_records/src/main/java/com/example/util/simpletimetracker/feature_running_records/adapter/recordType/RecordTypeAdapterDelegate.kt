package com.example.util.simpletimetracker.feature_running_records.adapter.recordType

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.extension.setOnLongClickWith
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.R
import kotlinx.android.synthetic.main.item_record_type_layout.view.*

class RecordTypeAdapterDelegate(
    private val onItemClick: ((RecordTypeViewData) -> Unit),
    private val onItemLongClick: ((RecordTypeViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RecordTypeViewHolder(parent)

    inner class RecordTypeViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_record_type_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView.viewRecordTypeItem) {
            item as RecordTypeViewData

            itemColor = item.color
            itemIcon = item.iconId
            itemName = item.name
            setOnClickWith(item, onItemClick)
            setOnLongClickWith(item, onItemLongClick)
        }
    }
}