package com.example.util.simpletimetracker.feature_running_records.adapter.recordType

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_running_records.R
import kotlinx.android.synthetic.main.record_type_item_add_layout.view.*

class RecordTypeAddAdapterDelegate(
    private val onItemClick: (() -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        RunningRecordsViewHolder(parent)

    inner class RunningRecordsViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.record_type_item_add_layout) {

        override fun bind(item: ViewHolderType) = with(itemView) {
            item as RecordTypeAddViewData

            // TODO set icon and color here not in layout
            layoutRecordTypeItemAdd.setOnClickListener {
                onItemClick.invoke()
            }
        }
    }
}