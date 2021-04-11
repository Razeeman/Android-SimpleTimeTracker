package com.example.util.simpletimetracker.feature_change_record_type.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class ChangeRecordTypeEmojiViewData(
    val emojiText: String,
    @ColorInt val colorInt: Int
) : ViewHolderType {

    override fun getUniqueId(): Long = emojiText.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordTypeEmojiViewData
}