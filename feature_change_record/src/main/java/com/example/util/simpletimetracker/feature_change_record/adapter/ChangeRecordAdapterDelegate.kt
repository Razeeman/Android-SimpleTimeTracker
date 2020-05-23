package com.example.util.simpletimetracker.feature_change_record.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordTypeViewData
import kotlinx.android.synthetic.main.item_record_layout.view.*

class ChangeRecordAdapterDelegate(
    private val onItemClick: ((ChangeRecordTypeViewData) -> Unit)
    ) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordsViewHolder(parent)

    inner class RunningRecordsViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_record_layout) {

        override fun bind(item: ViewHolderType) = with(itemView.viewRecordTypeItem) {
            item as ChangeRecordTypeViewData

            color = item.color
            icon = item.icon
            name = item.name
            setOnClickWith(item, onItemClick)
        }
    }
}