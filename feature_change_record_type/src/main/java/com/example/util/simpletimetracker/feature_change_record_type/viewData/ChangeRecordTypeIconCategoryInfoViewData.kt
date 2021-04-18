package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.model.EmojiType

data class ChangeRecordTypeIconCategoryInfoViewData(
    val type: EmojiType,
    val text: String
) : ViewHolderType {

    override fun getUniqueId(): Long = type.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ChangeRecordTypeIconCategoryInfoViewData
}