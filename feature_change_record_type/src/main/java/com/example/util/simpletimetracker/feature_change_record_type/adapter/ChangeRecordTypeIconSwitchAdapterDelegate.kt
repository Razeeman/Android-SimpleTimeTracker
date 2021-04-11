package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconSwitchViewData
import kotlinx.android.synthetic.main.change_record_type_item_icon_switch_layout.view.btnChangeRecordTypeIconSwitchItem

fun createChangeRecordTypeIconSwitchAdapterDelegate(
    onClick: ((ButtonsRowViewData) -> Unit)
) = createRecyclerAdapterDelegate<ChangeRecordTypeIconSwitchViewData>(
    R.layout.change_record_type_item_icon_switch_layout
) { itemView, item, _ ->

    with(itemView) {
        item as ChangeRecordTypeIconSwitchViewData

        btnChangeRecordTypeIconSwitchItem.adapter.replaceAsNew(item.data)
        btnChangeRecordTypeIconSwitchItem.listener = onClick
    }
}