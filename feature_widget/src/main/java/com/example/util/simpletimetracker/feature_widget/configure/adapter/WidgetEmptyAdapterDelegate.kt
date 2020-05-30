package com.example.util.simpletimetracker.feature_widget.configure.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.configure.viewData.WidgetEmptyViewData
import kotlinx.android.synthetic.main.item_widget_empty_layout.view.*

class WidgetEmptyAdapterDelegate() : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        WidgetEmptyViewHolder(parent)

    inner class WidgetEmptyViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_widget_empty_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as WidgetEmptyViewData

            tvWidgetEmptyItem.text = item.message
        }
    }
}