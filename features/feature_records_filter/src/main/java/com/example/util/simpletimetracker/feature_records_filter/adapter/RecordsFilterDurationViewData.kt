package com.example.util.simpletimetracker.feature_records_filter.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordsFilterDurationViewData(
    val id: Long,
    val durationFrom: String,
    val durationTo: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordsFilterDurationViewData

    enum class FieldType {
        FROM,
        TO,
    }
}