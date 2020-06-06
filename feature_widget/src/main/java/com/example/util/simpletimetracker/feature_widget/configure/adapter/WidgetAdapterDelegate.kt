package com.example.util.simpletimetracker.feature_widget.configure.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.feature_widget.R
import kotlinx.android.synthetic.main.item_widget_record_type_layout.view.*

class WidgetAdapterDelegate(
    private val onItemClick: ((RecordTypeViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        WidgetViewHolder(parent)

    inner class WidgetViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_widget_record_type_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView.viewRecordTypeItem) {
            item as RecordTypeViewData

            itemColor = item.color
            itemIcon = item.iconId
            itemName = item.name
            setOnClickWith(item, onItemClick)
        }
    }
}