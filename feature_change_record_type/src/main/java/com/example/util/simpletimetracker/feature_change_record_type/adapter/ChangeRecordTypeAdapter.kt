package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeColorViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData

class ChangeRecordTypeAdapter(
    onColorItemClick: ((ChangeRecordTypeColorViewData) -> Unit),
    onIconItemClick: ((ChangeRecordTypeIconViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = ChangeRecordTypeColorAdapterDelegate(onColorItemClick)
        delegates[ViewHolderType.VIEW2] = ChangeRecordTypeIconAdapterDelegate(onIconItemClick)
    }
}