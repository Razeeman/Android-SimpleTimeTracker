package com.example.util.simpletimetracker.feature_dialogs.chartFilter.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewData.ChartFilterRecordTypeViewData
import kotlinx.android.synthetic.main.item_chart_filter_record_type_layout.view.*

class ChartFilterAdapterDelegate(
    private val onItemClick: ((ChartFilterRecordTypeViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        ChartFilterViewHolder(parent)

    inner class ChartFilterViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_chart_filter_record_type_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView.viewRecordTypeItem) {
            item as ChartFilterRecordTypeViewData

            itemColor = item.color
            itemIcon = item.iconId
            itemName = item.name
            setOnClickWith(item, onItemClick)
        }
    }
}