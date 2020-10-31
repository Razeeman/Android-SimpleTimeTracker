package com.example.util.simpletimetracker.feature_records_all.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_records_all.R
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllDateViewData
import kotlinx.android.synthetic.main.item_records_all_date_layout.view.tvRecordsAllDate

class RecordAllDateAdapterDelegate : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        EmptyViewHolder(parent)

    inner class EmptyViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_records_all_date_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as RecordsAllDateViewData

            tvRecordsAllDate.text = item.message
        }
    }
}