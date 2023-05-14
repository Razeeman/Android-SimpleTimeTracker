package com.example.util.simpletimetracker.feature_records_filter.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordsFilterButtonViewData(
    val type: Type,
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = type.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordsFilterButtonViewData

    enum class Type {
        INVERT_SELECTION,
    }
}