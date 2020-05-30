package com.example.util.simpletimetracker.feature_widget.configure.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.configure.viewData.WidgetRecordTypeViewData
import kotlinx.android.synthetic.main.item_widget_record_type_layout.view.*

class WidgetAdapterDelegate(
    private val onItemClick: ((WidgetRecordTypeViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        WidgetViewHolder(parent)

    inner class WidgetViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_widget_record_type_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView.viewRecordTypeItem) {
            item as WidgetRecordTypeViewData

            color = item.color
            icon = item.iconId
            name = item.name
            setOnClickWith(item, onItemClick)
        }
    }
}