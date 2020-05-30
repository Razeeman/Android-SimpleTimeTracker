package com.example.util.simpletimetracker.feature_change_record_type.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import kotlinx.android.synthetic.main.change_record_type_item_icon_layout.view.*

class ChangeRecordTypeIconAdapterDelegate(
    private val onIconItemClick: ((ChangeRecordTypeIconViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        ChangeRecordTypeIconViewHolder(parent)

    inner class ChangeRecordTypeIconViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.change_record_type_item_icon_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as ChangeRecordTypeIconViewData

            layoutChangeRecordTypeIconItem.setCardBackgroundColor(item.colorInt)
            ivChangeRecordTypeIconItem.setBackgroundResource(item.iconResId)
            layoutChangeRecordTypeIconItem.setOnClickWith(item, onIconItemClick)
        }
    }
}