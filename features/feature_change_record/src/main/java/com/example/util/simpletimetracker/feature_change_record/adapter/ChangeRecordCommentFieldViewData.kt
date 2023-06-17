package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ChangeRecordCommentFieldViewData(
    val id: Long,
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordCommentFieldViewData
}