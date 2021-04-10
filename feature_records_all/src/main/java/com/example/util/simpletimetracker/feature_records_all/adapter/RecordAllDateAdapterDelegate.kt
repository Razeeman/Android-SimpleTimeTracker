package com.example.util.simpletimetracker.feature_records_all.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_records_all.R
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllDateViewData
import kotlinx.android.synthetic.main.item_records_all_date_layout.view.tvRecordsAllDate

fun createRecordAllDateAdapterDelegate() = createRecyclerAdapterDelegate<RecordsAllDateViewData>(
    R.layout.item_records_all_date_layout
) { itemView, item, _ ->

    with(itemView) {
        item as RecordsAllDateViewData

        tvRecordsAllDate.text = item.message
    }
}