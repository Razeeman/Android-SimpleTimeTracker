package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

sealed class RecordTypeIconParams : Parcelable {
    @Parcelize
    data class Image(@DrawableRes val iconId: Int) : RecordTypeIconParams()

    @Parcelize
    data class Emoji(val emojiText: String) : RecordTypeIconParams()
}