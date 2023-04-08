package com.example.util.simpletimetracker.feature_records_filter.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordsFilterCommentViewData(
    val id: Long,
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordsFilterCommentViewData
}