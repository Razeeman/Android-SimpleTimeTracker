package com.example.util.simpletimetracker.feature_running_records.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RunningRecordEmptyDividerViewData(
    val id: Long,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RunningRecordEmptyDividerViewData
}