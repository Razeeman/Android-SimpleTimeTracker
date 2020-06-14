package com.example.util.simpletimetracker.feature_running_records.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

object RunningRecordDividerViewData : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.DIVIDER

    // Only one item on screen
    override fun getUniqueId(): Long? = 1L
}