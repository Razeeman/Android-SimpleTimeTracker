package com.example.util.simpletimetracker.feature_records_filter.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordsFilterRangeViewData(
    val id: Long,
    val timeStarted: String,
    val timeEnded: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordsFilterRangeViewData
}