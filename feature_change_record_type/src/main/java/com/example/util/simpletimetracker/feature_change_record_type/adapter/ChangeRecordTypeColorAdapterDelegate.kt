package com.example.util.simpletimetracker.feature_change_record_type.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeColorViewData
import kotlinx.android.synthetic.main.change_record_type_item_color_layout.view.*

class ChangeRecordTypeColorAdapterDelegate(
    private val onColorItemClick: ((ChangeRecordTypeColorViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordsViewHolder(parent)

    inner class RunningRecordsViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.change_record_type_item_color_layout) {

        override fun bind(item: ViewHolderType) = with(itemView) {
            item as ChangeRecordTypeColorViewData

            layoutChangeRecordTypeColorItem.setCardBackgroundColor(item.colorInt)
            layoutChangeRecordTypeColorItem.setOnClickWith(item, onColorItemClick)
        }
    }
}