package com.example.util.simpletimetracker.core.viewData

import androidx.annotation.DrawableRes

sealed class RecordTypeIcon {
    data class Image(@DrawableRes val iconId: Int) : RecordTypeIcon()
    data class Emoji(val emojiText: String) : RecordTypeIcon()
}
