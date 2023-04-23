package com.example.util.simpletimetracker.feature_base_adapter.recordFilter

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordFilterViewData(
    val id: Long,
    val type: Type,
    val name: String,
    @ColorInt val color: Int,
    val selected: Boolean,
    val removeBtnVisible: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordFilterViewData

    enum class Type {
        ACTIVITY,
        CATEGORY,
        COMMENT,
        DATE,
        SELECTED_TAGS,
        FILTERED_TAGS,
        MANUALLY_FILTERED,
    }
}