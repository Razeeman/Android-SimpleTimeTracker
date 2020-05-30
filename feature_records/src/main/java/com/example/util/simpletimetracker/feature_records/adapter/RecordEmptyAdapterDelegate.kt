package com.example.util.simpletimetracker.feature_records.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.viewData.RecordEmptyViewData
import kotlinx.android.synthetic.main.item_record_empty_layout.view.*

class RecordEmptyAdapterDelegate() : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RecordEmptyViewHolder(parent)

    inner class RecordEmptyViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_record_empty_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as RecordEmptyViewData

            tvRecordEmptyItem.text = item.message
        }
    }
}