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
        ViewHolder(parent)

    inner class ViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_chart_filter_record_type_layout) {

        override fun bind(item: ViewHolderType) = with(itemView.viewRecordTypeItem) {
            item as ChartFilterRecordTypeViewData

            color = item.color
            icon = item.iconId
            name = item.name
            setOnClickWith(item, onItemClick)
        }
    }
}