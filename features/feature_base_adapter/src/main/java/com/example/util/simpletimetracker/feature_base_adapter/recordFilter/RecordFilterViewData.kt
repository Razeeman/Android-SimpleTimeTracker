package com.example.util.simpletimetracker.feature_base_adapter.recordFilter

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordFilterViewData(
    val type: Type,
    val name: String,
    @ColorInt val color: Int,
    val removeBtnVisible: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = type.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordFilterViewData

    enum class Type {
        ACTIVITY,
        COMMENT,
        DATE,
        SELECTED_TAGS,
        FILTERED_TAGS,
    }
}