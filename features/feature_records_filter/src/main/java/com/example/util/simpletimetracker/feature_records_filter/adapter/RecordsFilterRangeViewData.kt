package com.example.util.simpletimetracker.feature_records_filter.adapter

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordsFilterRangeViewData(
    val id: Long,
    val timeStarted: String,
    val timeStartedHint: String,
    val timeEnded: String,
    val timeEndedHint: String,
    val gravity: Gravity,
    @ColorInt val textColor: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordsFilterRangeViewData

    enum class FieldType {
        TIME_STARTED,
        TIME_ENDED,
    }

    enum class Gravity {
        CENTER,
        CENTER_VERTICAL,
    }
}