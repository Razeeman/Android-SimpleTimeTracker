package com.example.util.simpletimetracker.core.adapter.recordType

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.dpToPx
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import kotlinx.android.synthetic.main.item_record_type_layout.view.*

class RecordTypeAdapterDelegate(
    private val onItemClick: ((RecordTypeViewData) -> Unit)? = null
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        ChartFilterViewHolder(parent)

    inner class ChartFilterViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_record_type_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ): Unit = with(itemView.viewRecordTypeItem) {
            item as RecordTypeViewData

            layoutParams = layoutParams.also { params ->
                item.width?.dpToPx()?.let { params.width = it }
                item.height?.dpToPx()?.let { params.height = it }
            }

            itemIsRow = item.asRow
            itemColor = item.color
            itemIcon = item.iconId
            itemIconColor = item.iconColor
            itemName = item.name
            onItemClick?.let { setOnClickWith(item, onItemClick) }
        }
    }
}