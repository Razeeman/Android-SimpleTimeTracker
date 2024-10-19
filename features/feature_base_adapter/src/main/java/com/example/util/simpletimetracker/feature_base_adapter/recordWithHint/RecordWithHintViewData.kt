package com.example.util.simpletimetracker.feature_base_adapter.recordWithHint

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData

data class RecordWithHintViewData(
    val record: RecordViewData.Tracked,
) : ViewHolderType {

    override fun getUniqueId(): Long = record.id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordWithHintViewData
}