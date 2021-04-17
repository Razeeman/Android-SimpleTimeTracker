package com.example.util.simpletimetracker.core.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class EmojiViewData(
    val emojiText: String,
    val emojiCodes: List<Int>,
    @ColorInt val colorInt: Int
) : ViewHolderType {

    override fun getUniqueId(): Long = emojiText.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is EmojiViewData
}