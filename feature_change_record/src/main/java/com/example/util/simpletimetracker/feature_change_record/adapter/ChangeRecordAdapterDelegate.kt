package com.example.util.simpletimetracker.feature_change_record.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.dpToPx
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record.R
import kotlinx.android.synthetic.main.item_change_record_type_layout.view.*

class ChangeRecordAdapterDelegate(
    private val onItemClick: ((RecordTypeViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        ChangeRecordViewHolder(parent)

    inner class ChangeRecordViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_change_record_type_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView.viewChangeRecordTypeItem) {
            item as RecordTypeViewData

            layoutParams = layoutParams.also { params ->
                item.width?.dpToPx()?.let { params.width = it}
                item.height?.dpToPx()?.let { params.height = it}
            }

            itemIsRow = item.asRow
            itemColor = item.color
            itemIcon = item.iconId
            itemName = item.name
            setOnClickWith(item, onItemClick)
        }
    }
}