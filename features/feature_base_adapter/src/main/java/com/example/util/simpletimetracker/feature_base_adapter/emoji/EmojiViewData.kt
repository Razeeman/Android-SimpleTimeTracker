package com.example.util.simpletimetracker.feature_base_adapter.emoji

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class EmojiViewData(
    val emojiText: String,
    val emojiCodes: String,
    @ColorInt val colorInt: Int
) : ViewHolderType {

    override fun getUniqueId(): Long = emojiText.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is EmojiViewData
}