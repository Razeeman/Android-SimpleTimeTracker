package com.example.util.simpletimetracker.feature_dialogs.cardOrder.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.dpToPx
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.feature_dialogs.R
import kotlinx.android.synthetic.main.item_record_type_layout.view.*

class CardOrderAdapterDelegate : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        CardOrderViewHolder(parent)

    inner class CardOrderViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_record_type_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView.viewRecordTypeItem) {
            item as RecordTypeViewData

            layoutParams = layoutParams.also { params ->
                item.width?.dpToPx()?.let { params.width = it}
                item.height?.dpToPx()?.let { params.height = it}
            }

            itemIsRow = item.asRow
            itemColor = item.color
            itemIcon = item.iconId
            itemName = item.name
        }
    }
}