package com.example.util.simpletimetracker.feature_running_records.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordTypeAddViewData
import kotlinx.android.synthetic.main.item_running_record_type_layout.view.*

class RunningRecordTypeAddAdapterDelegate(
    private val onItemClick: (() -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordTypeAddViewHolder(parent)

    inner class RunningRecordTypeAddViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_running_record_type_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView.viewRecordTypeItem) {
            item as RunningRecordTypeAddViewData

            itemColor = item.color
            itemIcon = item.iconId
            itemName = item.name
            setOnClick(onItemClick)
        }
    }
}